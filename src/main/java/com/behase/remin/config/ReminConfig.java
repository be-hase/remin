package com.behase.remin.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Data
public class ReminConfig {
    private String host;
    private AuthConfig auth = new AuthConfig();
    private ServerConfig server = new ServerConfig();
    private RedisConfig redis = new RedisConfig();
    private SchedulerConfig scheduler = new SchedulerConfig();
    private NoticeConfig notice = new NoticeConfig();
    private OutputMetricsConfig outputMetrics = new OutputMetricsConfig();

    private List<String> errors = Lists.newArrayList();

    public static ReminConfig create(String configLocation) throws JsonParseException, JsonMappingException,
            IOException {
        if (Paths.get(configLocation).toFile().exists()) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(Paths.get(configLocation).toFile(), ReminConfig.class);
        } else {
            ClassPathResource configResource = new ClassPathResource(configLocation);
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(configResource.getInputStream(), ReminConfig.class);
        }
    }

    public Properties getProperties() {
        validate();

        Properties prop = new Properties();

        //remin
        prop.setProperty(
                "remin.host",
                StringUtils.defaultString(host));

        // auth
        prop.setProperty(
                "auth.enabled",
                StringUtils.defaultIfBlank(
                        auth.getEnabled(),
                        AuthConfig.DEFAULT_ENABLED));
        prop.setProperty(
                "auth.allowAnonymous",
                StringUtils.defaultIfBlank(
                        auth.getAllowAnonymous(),
                        AuthConfig.DEFAULT_ALLOW_ANONYMOUS));

        // server
        prop.setProperty(
                "server.port",
                StringUtils.defaultIfBlank(
                        server.getPort(),
                        ServerConfig.DEFAULT_PORT));
        prop.setProperty(
                "management.port",
                StringUtils.defaultIfBlank(
                        server.getMonitorPort(),
                        ServerConfig.DEFAULT_MONITOR_PORT));

        // redis
        prop.setProperty(
                "redis.prefixKey",
                StringUtils.defaultIfBlank(
                        redis.getPrefixKey(),
                        RedisConfig.DEFAULT_PREFIX_KEY));
        prop.setProperty(
                "redis.host",
                redis.getHost());
        prop.setProperty(
                "redis.port",
                redis.getPort());
        prop.setProperty(
                "redis.password",
                StringUtils.defaultIfBlank(
                        redis.getPassword(),
                        RedisConfig.DEFAULT_PASSWORD));

        // scheduler
        prop.setProperty(
                "scheduler.collectStaticsInfoIntervalMillis",
                StringUtils.defaultIfBlank(
                        scheduler.getCollectStaticsInfoIntervalMillis(),
                        SchedulerConfig.DEFAULT_COLLECT_STATICS_INFO_INTERVAL_MILLIS));
        prop.setProperty(
                "scheduler.collectStaticsInfoMaxCount",
                StringUtils.defaultIfBlank(
                        scheduler.getCollectStaticsInfoMaxCount(),
                        SchedulerConfig.DEFAULT_COLLECT_STATICS_INFO_MAX_COUNT));
        prop.setProperty(
                "scheduler.collectSlowLogMaxCount",
                StringUtils.defaultIfBlank(
                        scheduler.getCollectSlowLogMaxCount(),
                        SchedulerConfig.DEFAULT_COLLECT_SLOW_LOG_MAX_COUNT));

        // notice email
        prop.setProperty(
                "notice.mail.host",
                StringUtils.defaultIfBlank(
                        notice.getMail().getHost(),
                        NoticeConfig.NoticeMailConfig.DEFAULT_HOST));
        prop.setProperty(
                "notice.mail.port",
                StringUtils.defaultIfBlank(
                        notice.getMail().getPort(),
                        NoticeConfig.NoticeMailConfig.DEFAULT_PORT));
        prop.setProperty(
                "notice.mail.user",
                StringUtils.defaultIfBlank(
                        notice.getMail().getUser(),
                        NoticeConfig.NoticeMailConfig.DEFAULT_USER));
        prop.setProperty(
                "notice.mail.password",
                StringUtils.defaultIfBlank(
                        notice.getMail().getPassword(),
                        NoticeConfig.NoticeMailConfig.DEFAULT_PASSWORD));
        prop.setProperty(
                "notice.mail.from",
                StringUtils.defaultIfBlank(
                        notice.getMail().getFrom(),
                        NoticeConfig.NoticeMailConfig.DEFAULT_FROM));
        prop.setProperty(
                "notice.mail.charset",
                StringUtils.defaultIfBlank(
                        notice.getMail().getCharset(),
                        NoticeConfig.NoticeMailConfig.DEFAULT_CHARSET));

        // output metrics
        // fluentd
        prop.setProperty(
                "outputMetrics.fluentd.enabled",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getEnabled(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_ENABLED));
        prop.setProperty(
                "outputMetrics.fluentd.host",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getHost(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_HOST));
        prop.setProperty(
                "outputMetrics.fluentd.port",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getPort(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_PORT));
        prop.setProperty(
                "outputMetrics.fluentd.timeout",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getTimeout(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_TIMEOUT));
        prop.setProperty(
                "outputMetrics.fluentd.bufferCapacity",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getBufferCapacity(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_BUFFER_CAPACITY));
        prop.setProperty(
                "outputMetrics.fluentd.tag",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getTag(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_TAG));
        prop.setProperty(
                "outputMetrics.fluentd.nodeTag",
                StringUtils.defaultIfBlank(
                        outputMetrics.getFluentd().getNodeTag(),
                        OutputMetricsConfig.OutputMetricsFluentdConfig.DEFAULT_NODE_TAG));

        return prop;
    }

    private void validate() {
        List<String> errors = Lists.newArrayList();

        // server
        if (server.getPort() != null) {
            check(isNumeric(server.getPort()), "'server.port' must be numeric.");
        }
        if (server.getMonitorPort() != null) {
            check(isNumeric(server.getMonitorPort()), "'server.monitorPort' must be numeric.");
        }

        // auth

        // redis
        check(StringUtils.isNotBlank(redis.getHost()), "'redis.host' is blank.");
        check(StringUtils.isNotBlank(redis.getPort()), "'redis.port' is blank.");
        check(isNumeric(redis.getPort()), "'redis.port' must be numeric.");

        // scheduler
        if (scheduler.getCollectStaticsInfoIntervalMillis() != null) {
            check(isInteger(scheduler.getCollectStaticsInfoIntervalMillis()), "'scheduler.collectStaticsInfoIntervalMillis' must be integer.");
        }
        if (scheduler.getCollectStaticsInfoMaxCount() != null) {
            check(isInteger(scheduler.getCollectStaticsInfoMaxCount()), "'scheduler.collectStaticsInfoMaxCount' must be numeric.");
        }

        // notice
        // notice email
        if (notice.getMail().getPort() != null) {
            check(isNumeric(notice.getMail().getPort()), "'notice.email.port' must be numeric.");
        }

        // output metrics
        // fluentd
        if (outputMetrics.getFluentd().getTimeout() != null) {
            check(isInteger(outputMetrics.getFluentd().getTimeout()), "'outputMetcis.fluentd.timeout' must be integer.");
        }
        if (outputMetrics.getFluentd().getBufferCapacity() != null) {
            check(isInteger(outputMetrics.getFluentd().getBufferCapacity()), "'outputMetcis.fluentd.bufferCapacity' must be integer.");
        }

        if (errors.size() > 0) {
            throw new IllegalStateException(Joiner.on(",").join(errors));
        }
    }

    private void check(boolean bool, String error) {
        if (!bool) {
            errors.add(error);
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumeric(String str) {
        return StringUtils.isNumeric(str);
    }
}
