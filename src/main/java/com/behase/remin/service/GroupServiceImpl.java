package com.behase.remin.service;

import com.behase.remin.Constants;
import com.behase.remin.exception.InvalidParameterException;
import com.behase.remin.model.*;
import com.behase.remin.util.JedisUtils;
import com.behase.remin.util.ValidationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    JedisPool dataStoreJedisPool;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    NodeService nodeService;

    @Value("${redis.prefixKey}")
    private String redisPrefixKey;

    @Override
    public Set<String> getGroups() {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            return dataStoreJedis.smembers(Constants.getGroupsRedisKey(redisPrefixKey));
        }
    }

    @Override
    public Group getGroup(String groupName) throws IOException {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            String storeNodesJson = dataStoreJedis.get(Constants.getGroupRedisKey(redisPrefixKey, groupName));
            List<StoreNode> storeNodes = mapper.readValue(storeNodesJson, new TypeReference<List<StoreNode>>() {
            });
            if (storeNodes.isEmpty()) {
                throw new InvalidParameterException(String.format("%s does not exists.", groupName));
            }

            List<Node> nodes = storeNodes.stream().map(storeNode -> {
                Node node = new Node();
                node.setHostAndPort(storeNode.getHostAndPort());
                node.setPassword(StringUtils.defaultIfBlank(storeNode.getPassword(), ""));
                try (Jedis jedis = JedisUtils.getJedisByHostAndPort(storeNode.getHostAndPort())) {
                    if (StringUtils.isNotBlank(storeNode.getPassword())) {
                        jedis.auth(storeNode.getPassword());
                    }
                    jedis.ping();
                    node.setConnected(true);
                } catch (Exception e) {
                    log.error(String.format("Cannot connect to %s", storeNode.getHostAndPort()), e);
                    node.setConnected(false);
                }
                return node;
            }).collect(Collectors.toList());

            Group group = new Group();
            group.setGroupName(groupName);
            group.setNodes(nodes);
            return group;
        }
    }

    @Override
    public Group getGroupWithHiddenPassword(String groupName) throws IOException {
        Group group = getGroup(groupName);
        List<Node> nodes = Lists.newArrayList(group.getNodes());
        nodes.forEach(node -> {
            if (StringUtils.isNotBlank(node.getPassword())) {
                StringBuilder builder = new StringBuilder();
                IntStream.range(0, node.getPassword().length()).forEach(v -> {
                    builder.append("x");
                });
                node.setPassword(builder.toString());
            }
        });
        group.setNodes(nodes);
        return group;
    }

    @Override
    public boolean existsGroupName(String groupName) {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            Set<String> groupNames = dataStoreJedis.smembers(Constants.getGroupsRedisKey(redisPrefixKey));
            return groupNames.contains(groupName);
        }
    }

    @Override
    public void setGroup(String groupName, List<String> hostAndPorts, String password) throws JsonProcessingException {
        ValidationUtils.groupName(groupName);
        Set<String> hostAndPortsSet = Sets.newTreeSet(hostAndPorts);

        hostAndPortsSet.forEach(hostAndPort -> {
            try (Jedis jedis = JedisUtils.getJedisByHostAndPort(hostAndPort)) {
                if (StringUtils.isNotBlank(password)) {
                    jedis.auth(password);
                }
                jedis.ping();
            } catch (Exception e) {
                log.error(String.format("Cannot connect to %s", hostAndPort), e);
                throw new InvalidParameterException(String.format("Cannot connect to %s", hostAndPort));
            }
        });

        List<StoreNode> storeNodes = hostAndPortsSet.stream().map(v -> {
            List<String> hostAndPortList = Splitter.on(":").trimResults().splitToList(v);

            StoreNode storeNode = new StoreNode();
            storeNode.setHostAndPort(Joiner.on(":").join(hostAndPortList));
            storeNode.setPassword(StringUtils.defaultIfBlank(password, ""));
            return storeNode;
        }).collect(Collectors.toList());

        storeNodes.sort((v1, v2) -> v1.getHostAndPort().compareTo(v2.getHostAndPort()));

        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            Pipeline p = dataStoreJedis.pipelined();
            p.sadd(Constants.getGroupsRedisKey(redisPrefixKey), groupName);
            p.set(Constants.getGroupRedisKey(redisPrefixKey, groupName), mapper.writeValueAsString(storeNodes));
            p.sync();
        }
    }

    @Override
    public void changeGroupName(String groupName, String newGroupName) throws IOException {
        ValidationUtils.groupName(newGroupName);
        if (!existsGroupName(groupName)) {
            throw new InvalidParameterException(String.format("Group name(%s) does not exists. Please confirm.", groupName));
        }
        if (existsGroupName(newGroupName)) {
            throw new InvalidParameterException(String.format("New group name(%s) already exists. Please confirm.", newGroupName));
        }

        String groupPrefixKey = Constants.getGroupRedisKey(redisPrefixKey, groupName);
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            Set<String> currentKeys = Sets.newHashSet(groupPrefixKey);
            currentKeys.addAll(dataStoreJedis.keys(groupPrefixKey + ".*"));

            Pipeline p = dataStoreJedis.pipelined();
            currentKeys.forEach(currentKey -> {
                String newKey = Constants.getGroupRedisKey(redisPrefixKey, newGroupName) + StringUtils.removeStart(currentKey, groupPrefixKey);
                p.rename(currentKey, newKey);
            });
            p.sadd(Constants.getGroupsRedisKey(redisPrefixKey), newGroupName);
            p.srem(Constants.getGroupsRedisKey(redisPrefixKey), groupName);
            p.sync();
        }
    }

    @Override
    public void addGroupNodes(String groupName, List<String> hostAndPorts, String password) throws IOException {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            if (!existsGroupName(groupName)) {
                throw new InvalidParameterException(String.format("Not exists groupName. (%s)", groupName));
            }

            String existStoreNodesJson = dataStoreJedis.get(Constants.getGroupRedisKey(redisPrefixKey, groupName));
            List<StoreNode> existStoreNodes = mapper.readValue(existStoreNodesJson, new TypeReference<List<StoreNode>>() {
            });

            hostAndPorts.forEach(hostAndPort -> {
                boolean alreadyExists = existStoreNodes.stream().filter(existStoreNode -> StringUtils.equals(existStoreNode.getHostAndPort(), hostAndPort)).findAny().isPresent();

                if (!alreadyExists) {
                    try (Jedis jedis = JedisUtils.getJedisByHostAndPort(hostAndPort)) {
                        if (StringUtils.isNotBlank(password)) {
                            jedis.auth(password);
                        }
                        jedis.ping();

                        StoreNode storeNode = new StoreNode();
                        storeNode.setHostAndPort(hostAndPort);
                        storeNode.setPassword(StringUtils.defaultIfBlank(password, ""));
                        existStoreNodes.add(storeNode);
                    } catch (Exception e) {
                        log.error(String.format("Cannot connect to %s", hostAndPort), e);
                        throw new InvalidParameterException(String.format("Cannot connect to %s", hostAndPort));
                    }
                }
            });

            existStoreNodes.sort((v1, v2) -> v1.getHostAndPort().compareTo(v2.getHostAndPort()));

            dataStoreJedis.set(Constants.getGroupRedisKey(redisPrefixKey, groupName), mapper.writeValueAsString(existStoreNodes));
        }
    }

    @Override
    public void deleteGroupNode(String groupName, String hostAndPort) throws IOException {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            if (!existsGroupName(groupName)) {
                throw new InvalidParameterException(String.format("Not exists groupName. (%s)", groupName));
            }

            String existStoreNodesJson = dataStoreJedis.get(Constants.getGroupRedisKey(redisPrefixKey, groupName));
            List<StoreNode> existStoreNodes = mapper.readValue(existStoreNodesJson, new TypeReference<List<StoreNode>>() {
            });
            if (existStoreNodes.size() == 1) {
                throw new InvalidParameterException(String.format("This is last node. So you cannot delete this node."));
            }

            existStoreNodes.removeIf(v -> StringUtils.equals(v.getHostAndPort(), hostAndPort));

            Pipeline p = dataStoreJedis.pipelined();
            p.set(Constants.getGroupRedisKey(redisPrefixKey, groupName), mapper.writeValueAsString(existStoreNodes));
            p.del(Constants.getNodeStaticsInfoRedisKey(redisPrefixKey, groupName, hostAndPort));
            p.sync();
        }
    }

    @Override
    public Notice getGroupNotice(String groupName) throws IOException {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            String result = dataStoreJedis.get(Constants.getGroupNoticeRedisKey(redisPrefixKey, groupName));
            if (StringUtils.isBlank(result)) {
                return null;
            }
            return mapper.readValue(result, Notice.class);
        }
    }

    @Override
    public void setGroupNotice(String groupName, Notice notice) throws IOException {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            String json = mapper.writeValueAsString(notice);
            dataStoreJedis.set(Constants.getGroupNoticeRedisKey(redisPrefixKey, groupName), json);
        }
    }

    @Override
    public void deleteGroup(String groupName) {
        try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
            Set<String> keys = dataStoreJedis.keys(Constants.getGroupRedisKey(redisPrefixKey, groupName) + ".*");

            Pipeline p = dataStoreJedis.pipelined();
            p.srem(Constants.getGroupsRedisKey(redisPrefixKey), groupName);
            p.del(Constants.getGroupRedisKey(redisPrefixKey, groupName));
            p.del(Constants.getGroupNoticeRedisKey(redisPrefixKey, groupName));
            if (keys.size() > 0) {
                p.del(keys.toArray(new String[keys.size()]));
            }
            p.sync();
        }
    }

    @Override
    public Map<String, Map<String, List<List<Object>>>> getGroupStaticsInfoHistory(String groupName,
                                                                                   List<String> hostAndPorts, List<String> fields, long start, long end) {
        if (end < start) {
            throw new InvalidParameterException("End time must be larger than start time.");
        }

        Map<String, Map<String, List<List<Object>>>> result = Maps.newLinkedHashMap();

        hostAndPorts.parallelStream().forEach(hostAndPort -> {
            log.debug("hostAndPort loop : {}", hostAndPort);
            Map<String, List<List<Object>>> staticsInfoHistory = nodeService.getStaticsInfoHistory(groupName, hostAndPort, fields, start, end);
            if (!staticsInfoHistory.isEmpty()) {
                result.put(hostAndPort, staticsInfoHistory);
            }
        });

        return result;
    }

    @Override
    public PagerData<SlowLog> getGroupSlowLogHistory(String groupName, long offset, long limit) {
        try (Jedis jedis = dataStoreJedisPool.getResource()) {
            String key = Constants.getGroupSlowLogRedisKey(redisPrefixKey, groupName);
            List<String> strs = jedis.lrange(key, offset, offset + limit - 1);
            long total = jedis.llen(key);
            List<SlowLog> slowLogs = strs.stream().map(v -> {
                try {
                    return mapper.readValue(v, SlowLog.class);
                } catch (IOException e) {
                    return null;
                }
            }).filter(v -> v != null).collect(Collectors.toList());

            return new PagerData(offset, limit, total, slowLogs);
        }
    }
}
