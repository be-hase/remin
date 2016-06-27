package com.behase.remin.config;

import lombok.Data;

@Data
public class RedisConfig {
    public static final String DEFAULT_PREFIX_KEY = "_remin";
    public static final String DEFAULT_PASSWORD = "";

    private String prefixKey;
    private String host;
    private String port;
    private String password;
}
