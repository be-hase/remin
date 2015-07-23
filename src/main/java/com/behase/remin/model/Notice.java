package com.behase.remin.model;

import java.util.List;

import com.behase.relumin.model.NoticeHttp;
import com.behase.relumin.model.NoticeItem;
import com.behase.relumin.model.NoticeMail;
import com.google.common.collect.Lists;

import lombok.Data;

@Data
public class Notice {
	private NoticeMail mail = new NoticeMail();
	private NoticeHttp http = new NoticeHttp();
	private String invalidEndTime;
	private List<NoticeItem> items = Lists.newArrayList();
}
