package com.behase.remin.service;

import com.behase.remin.model.SlowLog;

import java.util.List;
import java.util.Map;

public interface NodeService {
    Map<String, String> getStaticsInfo(String hostAndPort, String password);

    Map<String, List<List<Object>>> getStaticsInfoHistory(String groupName, String hostAndPort, List<String> fields,
                                                          long start, long end);

    List<SlowLog> getSlowLog(String hostAndPort, String password);
}
