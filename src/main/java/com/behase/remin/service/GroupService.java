package com.behase.remin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.behase.remin.model.Group;
import com.behase.remin.model.Notice;

public interface GroupService {
	Set<String> getGroups();

	Group getGroup(String groupName) throws IOException;

	boolean existsGroupName(String groupName);

	void setGroup(String groupName, List<String> hostAndPorts) throws IOException;

	Notice getGroupNotice(String groupName) throws IOException;

	void setGroupNotice(String groupName, Notice notice) throws IOException;

	void deleteGroup(String groupName);

	Map<String, Map<String, List<List<Object>>>> getGroupStaticsInfoHistory(String groupName,
			List<String> hostAndPorts, List<String> fields, long start, long end);
}
