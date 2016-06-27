package com.behase.remin.service;

import com.behase.remin.model.Group;
import com.behase.remin.model.Notice;
import com.behase.remin.model.NoticeJob;

import java.util.List;

public interface NotifyService {
    void notify(Group cluster, Notice notice, List<NoticeJob> jobs);
}
