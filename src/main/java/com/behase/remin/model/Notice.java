package com.behase.remin.model;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class Notice {
    private NoticeMail mail = new NoticeMail();
    private NoticeHttp http = new NoticeHttp();
    private String invalidEndTime;
    private List<NoticeItem> items = Lists.newArrayList();
    private boolean notifyWhenDisconnected;
}
