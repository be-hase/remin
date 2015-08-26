package com.behase.remin.service;

import java.util.List;
import java.util.Map;

public interface NodeService {
	Map<String, String> getStaticsInfo(String hostAndPort, String password);

	Map<String, List<List<Object>>> getStaticsInfoHistory(String groupName, String hostAndPort, List<String> fields,
			long start, long end);
}
