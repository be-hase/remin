package com.behase.remin.webconfig;

import static com.behase.remin.config.NoticeConfig.NoticeMailConfig;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.fluentd.logger.FluentLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import redis.clients.jedis.JedisPool;

import com.behase.remin.interceptor.AddResponseHeaderInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import redis.clients.jedis.Protocol;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	@Autowired
	private AddResponseHeaderInterceptor addResponseHeaderInterceptor;

	@Value("${auth.enabled}")
	private boolean authEnabled;

	@Value("${redis.host}")
	private String redisHost;

	@Value("${redis.port}")
	private int redisPort;

	@Value("${redis.password}")
	private String redisPassword;

	@Value("${notice.mail.host}")
	private String noticeMailHost;

	@Value("${notice.mail.port}")
	private int noticeMailPort;

	@Value("${notice.mail.charset}")
	private String noticeMailCharset;

	@Value("${notice.mail.user}")
	private String noticeMailUser;

	@Value("${notice.mail.password}")
	private String noticeMailPassword;

	@Value("${outputMetrics.fluentd.enabled}")
	private boolean outputMetricsFluentdEnabled;

	@Value("${outputMetrics.fluentd.host}")
	private String outputMetricsFluentdHost;

	@Value("${outputMetrics.fluentd.port}")
	private int outputMetricsFluentdPort;

	@Value("${outputMetrics.fluentd.timeout}")
	private int outputMetricsFluentdTimeout;

	@Value("${outputMetrics.fluentd.bufferCapacity}")
	private int outputMetricsFluentdBufferCapacity;

	@Value("${outputMetrics.fluentd.tag}")
	private String outputMetricsFluentdTag;

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.registerModule(new AfterburnerModule());
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		objectMapper.setDateFormat(format);

		return objectMapper;
	}

	@Bean
	public FilterRegistrationBean characterEncodingFilterRegistrationBean() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);

		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/*");
		registrationBean.setOrder(0);

		return registrationBean;
	}

	@Bean(name = "datastoreRedis", destroyMethod = "destroy")
	public JedisPool jedisPool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(10);
		config.setMaxIdle(10);
		config.setMinIdle(5);
		config.setMaxWaitMillis(3000L);
		config.setTestOnBorrow(true);

		JedisPool pool;
		if (StringUtils.isBlank(redisPassword)) {
			pool = new JedisPool(config, redisHost, redisPort, Protocol.DEFAULT_TIMEOUT);
		} else {
			pool = new JedisPool(config, redisHost, redisPort, Protocol.DEFAULT_TIMEOUT, redisPassword);
		}

		return pool;
	}

	@Bean
	public MailSender JavaMailSenderImpl() {
		boolean notNotifyByMail = StringUtils.isBlank(noticeMailHost) || noticeMailPort == 0;
		if (notNotifyByMail) {
			return null;
		}

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(noticeMailHost);
		mailSender.setPort(noticeMailPort);
		mailSender.setDefaultEncoding(StringUtils.defaultString(noticeMailCharset, NoticeMailConfig.DEFAULT_CHARSET));
		if (StringUtils.isNotBlank(noticeMailUser)) {
			mailSender.setUsername(noticeMailUser);
			mailSender.setPassword(noticeMailPassword);
		}
		return mailSender;
	}

	@Bean(destroyMethod = "close")
	public FluentLogger clusterFluentLogger() {
		if (!outputMetricsFluentdEnabled || StringUtils.isBlank(outputMetricsFluentdHost)
			|| outputMetricsFluentdPort == 0) {
			return null;
		}

		return FluentLogger.getLogger(
			outputMetricsFluentdTag, outputMetricsFluentdHost,
			outputMetricsFluentdPort, outputMetricsFluentdTimeout,
			outputMetricsFluentdBufferCapacity);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(addResponseHeaderInterceptor);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		if (authEnabled) {
			registry.addViewController("/login").setViewName("login");
		}
	}
}
