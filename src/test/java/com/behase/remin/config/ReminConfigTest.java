package com.behase.remin.config;

import java.io.IOException;

import org.junit.Test;

import com.behase.remin.config.ReminConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReminConfigTest {
	@Test
	public void test() throws IOException {
		ReminConfig config = ReminConfig.create("remin-local-conf.yml");
		log.debug("config = {}", config);
	}
}
