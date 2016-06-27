package com.behase.remin.config;

import lombok.Data;

@Data
public class SchedulerConfig {
    public static final String DEFAULT_COLLECT_STATICS_INFO_INTERVAL_MILLIS = "120000";
    public static final String DEFAULT_COLLECT_STATICS_INFO_MAX_COUNT = "1500";

    private String collectStaticsInfoIntervalMillis;
    private String collectStaticsInfoMaxCount;
}
