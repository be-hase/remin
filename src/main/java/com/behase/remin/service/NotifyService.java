package com.behase.remin.service;

import java.util.List;

import com.behase.remin.model.Group;
import com.behase.remin.model.Notice;
import com.behase.remin.model.NoticeJob;

public interface NotifyService {
	void notify(Group cluster, Notice notice, List<NoticeJob> jobs);
}
