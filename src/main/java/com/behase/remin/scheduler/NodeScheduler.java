package com.behase.remin.scheduler;

import com.behase.remin.Constants;
import com.behase.remin.config.SchedulerConfig;
import com.behase.remin.exception.ApiException;
import com.behase.remin.model.*;
import com.behase.remin.service.GroupService;
import com.behase.remin.service.NodeService;
import com.behase.remin.service.NotifyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fluentd.logger.FluentLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.behase.remin.model.NoticeItem.NoticeOperator;
import static com.behase.remin.model.NoticeItem.NoticeValueType;
import static com.behase.remin.model.NoticeJob.ResultValue;

@Slf4j
@Component
@Profile(value = "!test")
public class NodeScheduler {
    @Autowired
    GroupService groupService;

    @Autowired
    NodeService nodeService;

    @Autowired
    JedisPool datastoreJedisPool;

    @Autowired
    NotifyService notifyService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    FluentLogger fluentLogger;

    @Value("${scheduler.collectStaticsInfoMaxCount:" + SchedulerConfig.DEFAULT_COLLECT_STATICS_INFO_MAX_COUNT + "}")
    private long collectStaticsInfoMaxCount;

    @Value("${scheduler.collectSlowLogMaxCount:" + SchedulerConfig.DEFAULT_COLLECT_SLOW_LOG_MAX_COUNT + "}")
    private long collectSlowLogMaxCount;

    @Value("${redis.prefixKey}")
    private String redisPrefixKey;

    @Value("${notice.mail.host}")
    private String noticeMailHost;

    @Value("${notice.mail.port}")
    private int noticeMailPort;

    @Value("${notice.mail.from}")
    private String noticeMailFrom;

    @Value("${outputMetrics.fluentd.nodeTag}")
    private String outputMetricsFluentdNodeTag;

    @Scheduled(fixedDelayString = "${scheduler.collectStaticsInfoIntervalMillis:"
            + SchedulerConfig.DEFAULT_COLLECT_STATICS_INFO_INTERVAL_MILLIS + "}")
    public void collectStaticsInfo() throws ApiException, IOException {
        log.info("collectStaticsInfo call");
        Set<String> groupNames = groupService.getGroups();
        for (String groupName : groupNames) {
            try {
                Notice notice = groupService.getGroupNotice(groupName);
                Group group = groupService.getGroup(groupName);
                List<Node> nodes = group.getNodes().stream().filter(node -> node.isConnected()).collect(Collectors.toList());
                Map<String, Map<String, String>> staticsInfos = Maps.newLinkedHashMap();
                List<SlowLog> slowLogs = Lists.newArrayList();

                for (Node node : nodes) {
                    try {
                        log.info("collectStaticsInfo groupName={}, hostAndPort={}", groupName, node.getHostAndPort());
                        Map<String, String> staticsInfo = nodeService.getStaticsInfo(node.getHostAndPort(), node.getPassword());
                        staticsInfos.put(node.getHostAndPort(), staticsInfo);

                        if (collectSlowLogMaxCount > 0) {
                            slowLogs.addAll(nodeService.getSlowLogAndReset(node.getHostAndPort(), node.getPassword()));
                        }

                        try (Jedis jedis = datastoreJedisPool.getResource()) {
                            String key = Constants.getNodeStaticsInfoRedisKey(redisPrefixKey, groupName, node.getHostAndPort());
                            jedis.lpush(key, mapper.writeValueAsString(staticsInfo));
                            jedis.ltrim(key, 0, collectStaticsInfoMaxCount - 1);
                        }
                    } catch (Exception e) {
                        log.error("collectStaticsInfo fail. groupName={}, hostAndPort={}", groupName, node.getHostAndPort(), e);
                    }
                }

                // sort slowLog, and save
                try {
                    saveSlowLogs(slowLogs, groupName);
                } catch (Exception e) {
                    log.error("saveSlowLogs fail. groupName={}", groupName, e);
                }

                // Output metrics
                try {
                    outputMetrics(group, staticsInfos);
                } catch (Exception e) {
                    log.error("outputMetrics fail. groupName={}", groupName, e);
                }

                // Notice
                if (notice == null) {
                    log.debug("No notice setting, so skip.");
                } else {
                    List<NoticeJob> noticeJobs = getNoticeJobs(notice, group, staticsInfos);
                    if (shouldNotifyDisconnected(notice, group) || !noticeJobs.isEmpty()) {
                        log.info("NOTIFY !! {}", noticeJobs);
                        notifyService.notify(group, notice, noticeJobs);
                    }
                }
            } catch (Exception e) {
                log.error("collectStaticsInfo fail. {}", groupName, e);
            }
        }
        log.info("collectStaticsInfo finish");
    }

    void saveSlowLogs(List<SlowLog> slowLogs, String groupName) {
        if (slowLogs.size() == 0) {
            return;
        }

        String key = Constants.getGroupSlowLogRedisKey(redisPrefixKey, groupName);

        slowLogs.sort((i, k) -> Long.compare(i.getTimeStamp(), k.getTimeStamp()));
        List<String> slowLogStrList = slowLogs.stream().map(v -> {
            try {
                return mapper.writeValueAsString(v);
            } catch (JsonProcessingException ignore) {
                return null;
            }
        }).filter(v -> v != null).collect(Collectors.toList());

        try (Jedis jedis = datastoreJedisPool.getResource()) {
            jedis.lpush(key, slowLogStrList.toArray(new String[slowLogs.size()]));
            jedis.ltrim(key, 0, collectSlowLogMaxCount - 1);
        }
    }

    public void outputMetrics(Group group, Map<String, Map<String, String>> staticsInfos) {
        if (fluentLogger != null) {
            // node metrics
            staticsInfos.forEach((hostAndPort, statics) -> {
                Map<String, Object> staticsObj = Maps.newHashMap();
                statics.forEach((k, v) -> {
                    staticsObj.put(k, v);
                });
                staticsObj.put("host_and_port", hostAndPort);
                log.debug("Logging on fluentd.");
                fluentLogger.log(
                        String.format("%s.%s", outputMetricsFluentdNodeTag, hostAndPort),
                        staticsObj);
            });
        }
    }

    boolean shouldNotifyDisconnected(Notice notice, Group group) {
        if (isInInvalidEndTime(notice)) {
            // ignore
            return false;
        }

        if (notice.isNotifyWhenDisconnected()) {
            List<String> disconnectedHostAndPorts = group.getNodes().stream().filter(node -> !node.isConnected()).map(Node::getHostAndPort).collect(Collectors.toList());
            if (!disconnectedHostAndPorts.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    List<NoticeJob> getNoticeJobs(Notice notice, Group group,
                                  Map<String, Map<String, String>> staticsInfos) {
        List<NoticeJob> noticeJobs = Lists.newArrayList();

        if (isInInvalidEndTime(notice)) {
            // ignore
            return noticeJobs;
        }

        for (NoticeItem item : notice.getItems()) {
            List<ResultValue> resultValues = Lists.newArrayList();

            staticsInfos.forEach((hostAndPort, staticsInfo) -> {
                String targetNodeInfoVal = staticsInfo.get(item.getMetricsName());

                if (shouldNotify(item.getValueType(), item.getOperator(), targetNodeInfoVal, item.getValue())) {
                    resultValues.add(new ResultValue(hostAndPort, targetNodeInfoVal));
                }
            });

            if (resultValues.size() > 0) {
                noticeJobs.add(new NoticeJob(item, resultValues));
            }
        }

        return noticeJobs;
    }

    boolean isInInvalidEndTime(Notice notice) {
        if (StringUtils.isNotBlank(notice.getInvalidEndTime())) {
            try {
                Long time = Long.valueOf(notice.getInvalidEndTime());
                if (System.currentTimeMillis() < time) {
                    log.info("NOW ignore notify. notice={}", notice);
                    return true;
                }
            } catch (Exception e) {
            }
        }

        return false;
    }

    boolean shouldNotify(String valueType, String operator, String value, String threshold) {
        if (value == null) {
            return false;
        }

        boolean isNotify = false;

        switch (NoticeValueType.getNoticeValueType(valueType)) {
            case STRING:
                switch (NoticeOperator.getNoticeOperator(operator)) {
                    case EQ:
                        isNotify = StringUtils.equalsIgnoreCase(value, threshold);
                        break;
                    case NE:
                        isNotify = !StringUtils.equalsIgnoreCase(value, threshold);
                        break;
                    default:
                        break;
                }
                break;
            case NUMBER:
                BigDecimal targetValNumber = new BigDecimal(value);
                int compareResult = targetValNumber.compareTo(new BigDecimal(threshold));
                switch (NoticeOperator.getNoticeOperator(operator)) {
                    case EQ:
                        isNotify = compareResult == 0;
                        break;
                    case NE:
                        isNotify = compareResult != 0;
                        break;
                    case GT:
                        isNotify = compareResult == 1;
                        break;
                    case GE:
                        isNotify = compareResult == 1 || compareResult == 0;
                        break;
                    case LT:
                        isNotify = compareResult == -1;
                        break;
                    case LE:
                        isNotify = compareResult == -1 || compareResult == 0;
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        return isNotify;
    }
}
