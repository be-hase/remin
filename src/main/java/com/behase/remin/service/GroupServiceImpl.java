package com.behase.remin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.behase.remin.Constants;
import com.behase.remin.exception.InvalidParameterException;
import com.behase.remin.model.Group;
import com.behase.remin.model.Node;
import com.behase.remin.model.Notice;
import com.behase.remin.util.JedisUtils;
import com.behase.remin.util.ValidationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

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
	public Group getGroup(String groupName) {
		try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
			List<String> hostAndPorts = dataStoreJedis.lrange(Constants.getGroupRedisKey(redisPrefixKey, groupName), 0, -1);
			if (hostAndPorts.isEmpty()) {
				throw new InvalidParameterException(String.format("%s does not exists.", groupName));
			}

			List<Node> nodes = hostAndPorts.stream().map(hostAndPort -> {
				Node node = new Node();
				node.setHostAndPort(hostAndPort);
				try (Jedis jedis = JedisUtils.getJedisByHostAndPort(hostAndPort)) {
					jedis.ping();
					node.setConnected(true);
				} catch (Exception e) {
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
	public boolean existsGroupName(String groupName) {
		try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
			Set<String> groupNames = dataStoreJedis.smembers(Constants.getGroupsRedisKey(redisPrefixKey));
			return groupNames.contains(groupName);
		}
	}

	@Override
	public void setGroup(String groupName, List<String> hostAndPorts) {
		ValidationUtils.groupName(groupName);
		Set<String> hostAndPortsSet = Sets.newTreeSet(hostAndPorts);

		hostAndPortsSet.forEach(hostAndPort -> {
			try (Jedis jedis = JedisUtils.getJedisByHostAndPort(hostAndPort)) {
				jedis.ping();
			} catch (Exception e) {
				throw new InvalidParameterException(String.format("Cannot connect to %s", hostAndPort));
			}
		});

		try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
			dataStoreJedis.sadd(Constants.getGroupsRedisKey(redisPrefixKey), groupName);
			dataStoreJedis.del(Constants.getGroupRedisKey(redisPrefixKey, groupName));
			dataStoreJedis.rpush(Constants.getGroupRedisKey(redisPrefixKey, groupName), hostAndPortsSet.toArray(new String[hostAndPortsSet.size()]));
		}
	}

	@Override
	public void addGroupNodes(String groupName, List<String> hostAndPorts) {
		try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
			if (!existsGroupName(groupName)) {
				throw new InvalidParameterException(String.format("Not exists groupName. (%s)", groupName));
			}
			List<String> existsHostAndPorts = dataStoreJedis.lrange(Constants.getGroupRedisKey(redisPrefixKey, groupName), 0, -1);

			hostAndPorts.forEach(hostAndPort -> {
				try (Jedis jedis = JedisUtils.getJedisByHostAndPort(hostAndPort)) {
					jedis.ping();
				} catch (Exception e) {
					throw new InvalidParameterException(String.format("Cannot connect to %s", hostAndPort));
				}
			});

			existsHostAndPorts.addAll(hostAndPorts);
			Set<String> hostAndPortsSet = Sets.newTreeSet(existsHostAndPorts);

			dataStoreJedis.del(Constants.getGroupRedisKey(redisPrefixKey, groupName));
			dataStoreJedis.rpush(Constants.getGroupRedisKey(redisPrefixKey, groupName), hostAndPortsSet.toArray(new String[hostAndPortsSet.size()]));
		}
	}

	@Override
	public void deleteGroupNode(String groupName, String hostAndPort) {
		try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
			if (!existsGroupName(groupName)) {
				throw new InvalidParameterException(String.format("Not exists groupName. (%s)", groupName));
			}

			List<String> existsHostAndPorts = dataStoreJedis.lrange(Constants.getGroupRedisKey(redisPrefixKey, groupName), 0, -1);
			if (existsHostAndPorts.size() == 1) {
				throw new InvalidParameterException(String.format("This is last node. So you cannot delete this node."));
			}

			existsHostAndPorts.removeIf(v -> StringUtils.equals(v, hostAndPort));
			Set<String> hostAndPortsSet = Sets.newTreeSet(existsHostAndPorts);

			dataStoreJedis.del(Constants.getGroupRedisKey(redisPrefixKey, groupName));
			dataStoreJedis.rpush(Constants.getGroupRedisKey(redisPrefixKey, groupName), hostAndPortsSet.toArray(new String[hostAndPortsSet.size()]));
			dataStoreJedis.del(Constants.getNodeStaticsInfoRedisKey(redisPrefixKey, groupName, hostAndPort));
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

			dataStoreJedis.srem(Constants.getGroupsRedisKey(redisPrefixKey), groupName);
			dataStoreJedis.del(Constants.getGroupRedisKey(redisPrefixKey, groupName));
			dataStoreJedis.del(Constants.getGroupNoticeRedisKey(redisPrefixKey, groupName));
			if (keys.size() > 0) {
				dataStoreJedis.del(keys.toArray(new String[keys.size()]));
			}
		}
	}

	@Override
	public Map<String, Map<String, List<List<Object>>>> getGroupStaticsInfoHistory(String groupName,
			List<String> hostAndPorts, List<String> fields, long start, long end) {
		if (end < start) {
			throw new InvalidParameterException("End time must be larger than start time.");
		}

		Map<String, Map<String, List<List<Object>>>> result = Maps.newLinkedHashMap();

		for (String hostAndPort : hostAndPorts) {
			log.debug("hostAndPort loop : {}", hostAndPort);
			Map<String, List<List<Object>>> staticsInfoHistory = nodeService.getStaticsInfoHistory(groupName, hostAndPort, fields, start, end);
			if (!staticsInfoHistory.isEmpty()) {
				result.put(hostAndPort, staticsInfoHistory);
			}
		}

		return result;
	}

	Group getGroupWithNoCheckConnection(String groupName) {
		try (Jedis dataStoreJedis = dataStoreJedisPool.getResource()) {
			List<String> hostAndPorts = dataStoreJedis.lrange(Constants.getGroupRedisKey(redisPrefixKey, groupName), 0, -1);

			List<Node> nodes = hostAndPorts.stream().map(hostAndPort -> {
				Node node = new Node();
				node.setHostAndPort(hostAndPort);
				return node;
			}).collect(Collectors.toList());

			Group group = new Group();
			group.setGroupName(groupName);
			group.setNodes(nodes);
			return group;
		}
	}
}
