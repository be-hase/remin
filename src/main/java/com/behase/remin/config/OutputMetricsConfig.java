package com.behase.remin.config;

import lombok.Data;

@Data
public class OutputMetricsConfig {
    private OutputMetricsFluentdConfig fluentd = new OutputMetricsFluentdConfig();

    @Data
    public static class OutputMetricsFluentdConfig {
        public static final String DEFAULT_ENABLED = "false";
        public static final String DEFAULT_HOST = "";
        public static final String DEFAULT_PORT = "0";
        public static final String DEFAULT_TIMEOUT = "3000";
        public static final String DEFAULT_BUFFER_CAPACITY = "1048576";
        public static final String DEFAULT_TAG = "remin";
        public static final String DEFAULT_NODE_TAG = "node";

        private String enabled;
        private String host;
        private String port;
        private String timeout;
        private String bufferCapacity;
        private String tag;
        private String nodeTag;
    }
}
