package com.behase.remin.util;

import com.behase.remin.exception.InvalidParameterException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class JedisUtils {
    private JedisUtils() {

    }

    public static Jedis getJedisByHostAndPort(String hostAndPort, int timeout) {
        String[] hostAndPortArray = StringUtils.split(hostAndPort, ":");
        return new Jedis(hostAndPortArray[0], Integer.valueOf(hostAndPortArray[1]), timeout);
    }

    public static Jedis getJedisByHostAndPort(String hostAndPort) {
        String[] hostAndPortArray = StringUtils.split(hostAndPort, ":");
        return new Jedis(hostAndPortArray[0], Integer.valueOf(hostAndPortArray[1]));
    }

    public static Map<String, String> parseInfoResult(String result) {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("_timestamp", String.valueOf(System.currentTimeMillis()));

        String[] line = StringUtils.split(result, "\n");
        for (String each : line) {
            String[] eachArray = StringUtils.split(each, ":");
            if (eachArray.length != 2) {
                continue;
            }
            String key = StringUtils.trim(eachArray[0]);
            String value = StringUtils.trim(eachArray[1]);
            map.put(key, value);
        }

        return map;
    }

    public static Set<String> getHostAndPorts(List<String> hostAndPortRanges) {
        Set<String> hostAndPorts = Sets.newTreeSet();

        hostAndPortRanges.forEach(v -> {
            v = StringUtils.trim(v);
            ValidationUtils.hostAndPortRange(v);
            String[] hostAndPortRangeArray = StringUtils.split(v, ":");

            String[] portRangeArray = StringUtils.split(hostAndPortRangeArray[1], "-");
            int start = Integer.valueOf(portRangeArray[0]);
            int end;
            if (portRangeArray.length > 1) {
                end = Integer.valueOf(portRangeArray[1]);
            } else {
                end = start;
            }
            if (start > end) {
                throw new InvalidParameterException(String.format("%s is invalid. start port must be equal or less than end port.", v));
            }
            for (int i = start; i <= end; i++) {
                hostAndPorts.add(new StringBuilder().append(hostAndPortRangeArray[0]).append(":").append(i).toString());
            }
        });

        return hostAndPorts;
    }
}
