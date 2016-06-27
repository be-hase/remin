package com.behase.remin;

import com.google.common.base.Joiner;

public class Constants {
    private Constants() {
    }

    public static final int ALL_SLOTS_SIZE = 16384;

    public static final String ERR_CODE_INVALID_PARAMETER = "400_000";
    public static final String ERR_CODE_REDIS_SET_FAILED = "500_000";
    public static final String ERR_CODE_UNKNOWN = "500_999";

    public static String getUsersRedisKey(String prefixKey) {
        return Joiner.on(".").join(prefixKey, "users").toString();
    }

    public static String getGroupsRedisKey(String prefixKey) {
        return Joiner.on(".").join(prefixKey, "groups").toString();
    }

    public static String getGroupRedisKey(String prefixKey, String groupName) {
        return Joiner.on(".").join(prefixKey, "group", groupName).toString();
    }

    public static String getGroupNoticeRedisKey(String prefixKey, String groupName) {
        return Joiner.on(".").join(prefixKey, "group", groupName, "notice").toString();
    }

    public static String getNodeRedisKey(String prefixKey, String groupName, String hostAndPort) {
        return Joiner.on(".").join(prefixKey, "group", groupName, "hostAndPort", hostAndPort).toString();
    }

    public static String getNodeStaticsInfoRedisKey(String prefixKey, String groupName, String hostAndPort) {
        return Joiner.on(".").join(prefixKey, "group", groupName, "hostAndPort", hostAndPort, "staticsInfo").toString();
    }
}
