package com.behase.remin;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.behase.remin.util.JedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
@WebIntegrationTest
public class ApiTest {
	@Value("${test.redis.normalStandAlone}")
	private String testRedisNormalStandAlone;

	@Value("${test.redis.normalStandAloneAll}")
	private String testRedisNormalStandAloneAll;

	@Value("${test.redis.emptyStandAlone}")
	private String testRedisEmptyStandAlone;

	@Value("${test.redis.emptyStandAloneAll}")
	private String testRedisEmptyStandAloneAll;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	JedisPool dataStoreJedisPool;

	@Autowired
	ObjectMapper mapper;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = webAppContextSetup(wac).build();
		clear();
	}

	private void clear() {
		try (Jedis jedis = dataStoreJedisPool.getResource()) {
			jedis.flushAll();
		} catch (Exception e) {
		}

		// empty (and reset)
		for (String item : StringUtils.split(testRedisEmptyStandAloneAll, ",")) {
			try (Jedis jedis = JedisUtils.getJedisByHostAndPort(item)) {
				try {
					jedis.flushAll();
				} catch (Exception e) {
				}
			} catch (Exception e) {
			}
		}
	}
}
