package com.behase.remin.scheduler;

import static com.behase.remin.model.NoticeItem.NoticeOperator;
import static com.behase.remin.model.NoticeItem.NoticeValueType;
import static com.behase.remin.model.NoticeJob.ResultValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.fluentd.logger.FluentLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.behase.remin.Constants;
import com.behase.remin.config.SchedulerConfig;
import com.behase.remin.exception.ApiException;
import com.behase.remin.model.Group;
import com.behase.remin.model.Node;
import com.behase.remin.model.Notice;
import com.behase.remin.model.NoticeItem;
import com.behase.remin.model.NoticeJob;
import com.behase.remin.service.GroupService;
import com.behase.remin.service.NodeService;
import com.behase.remin.service.NotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

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

				for (Node node : nodes) {
					try {
						log.info("collectStaticsInfo groupName={}, hostAndPort={}", groupName, node.getHostAndPort());
						Map<String, String> staticsInfo = nodeService.getStaticsInfo(node.getHostAndPort(), node.getPassword());
						staticsInfos.put(node.getHostAndPort(), staticsInfo);

						try (Jedis jedis = datastoreJedisPool.getResource()) {
							String key = Constants.getNodeStaticsInfoRedisKey(redisPrefixKey, groupName, node.getHostAndPort());
							jedis.lpush(key, mapper.writeValueAsString(staticsInfo));
							jedis.ltrim(key, 0, collectStaticsInfoMaxCount - 1);
						}
					} catch (Exception e) {
						log.error("collectStaticsIndo fail. groupName={}, hostAndPort={}", groupName, node.getHostAndPort(), e);
					}
				}

				outputMetrics(group, staticsInfos);

				if (notice != null) {
					checkThresholdAndPublishNotify(notice, group, staticsInfos);
				}
			} catch (Exception e) {
				log.error("collectStaticsIndo fail. {}", groupName, e);
			}
		}
		log.info("collectStaticsInfo finish");
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

	public void checkThresholdAndPublishNotify(Notice notice, Group group,
			Map<String, Map<String, String>> staticsInfos) {
		log.debug("checkThresholdAndPublishNotify call. notice={}, group={}", notice, group);
		if (StringUtils.isNotBlank(notice.getInvalidEndTime())) {
			try {
				Long time = Long.valueOf(notice.getInvalidEndTime());
				if (System.currentTimeMillis() < time) {
					log.debug("NOW ignore to notify.");
					return;
				}
			} catch (Exception e) {
			}
		}
		String from = StringUtils.isNotBlank(notice.getMail().getFrom()) ? notice.getMail().getFrom() : noticeMailFrom;
		boolean notNotifyByMail = StringUtils.isBlank(noticeMailHost) || noticeMailPort == 0
			|| StringUtils.isBlank(from);
		boolean notNotifyByHttp = StringUtils.isBlank(notice.getHttp().getUrl());
		if (notNotifyByMail && notNotifyByHttp) {
			if (notice.getItems().size() > 0) {
				log.warn("You set notification threshold, But mail or http is not set.");
			}
			return;
		}

		List<NoticeJob> noticeJobs = Lists.newArrayList();

		boolean notifyDisconnected = false;
		if (notice.isNotifyWhenDisconnected()) {
			List<String> disconnectedHostAndPorts = group.getNodes().stream().filter(node -> !node.isConnected()).map(Node::getHostAndPort).collect(Collectors.toList());
			if (!disconnectedHostAndPorts.isEmpty()) {
				notifyDisconnected = true;
			}
		}

		for (NoticeItem item : notice.getItems()) {
			List<ResultValue> resultValues = Lists.newArrayList();
			for (Entry<String, Map<String, String>> e : staticsInfos.entrySet()) {
				String hostAndPort = e.getKey();
				Map<String, String> staticsInfo = e.getValue();
				String targetNodeInfoVal = staticsInfo.get(item.getMetricsName());

				if (isNotify(item.getValueType(), item.getOperator(), targetNodeInfoVal, item.getValue())) {
					resultValues.add(new ResultValue(hostAndPort, targetNodeInfoVal));
				}
			}
			if (resultValues.size() > 0) {
				noticeJobs.add(new NoticeJob(item, resultValues));
			}
		}

		if (notifyDisconnected || !noticeJobs.isEmpty()) {
			log.info("NOTIFY !! {}", noticeJobs);
			notifyService.notify(group, notice, noticeJobs);
		}

		log.debug("checkThresholdAndPublishNotify finish");
	}

	public boolean isNotify(String valueType, String operator, String value, String threshold) {
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
