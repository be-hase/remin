package com.behase.remin.service;

import com.behase.remin.model.Group;
import com.behase.remin.model.Notice;
import com.behase.remin.model.PagerData;
import com.behase.remin.model.SlowLog;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupService {
    Set<String> getGroups();

    Group getGroup(String groupName) throws IOException;

    Group getGroupWithHiddenPassword(String groupName) throws IOException;

    boolean existsGroupName(String groupName);

    void setGroup(String groupName, List<String> hostAndPorts, String password) throws JsonProcessingException;

    void changeGroupName(String groupName, String newGroupName) throws IOException;

    void addGroupNodes(String groupName, List<String> hostAndPorts, String password) throws IOException;

    void deleteGroupNode(String groupName, String hostAndPort) throws IOException;

    Notice getGroupNotice(String groupName) throws IOException;

    void setGroupNotice(String groupName, Notice notice) throws IOException;

    void deleteGroup(String groupName);

    Map<String, Map<String, List<List<Object>>>> getGroupStaticsInfoHistory(String groupName,
                                                                            List<String> hostAndPorts, List<String> fields, long start, long end);

    PagerData<SlowLog> getGroupSlowLogHistory(String clusterName, long offset, long limit);
}
