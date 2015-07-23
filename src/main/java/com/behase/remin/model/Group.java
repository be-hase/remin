package com.behase.remin.model;

import java.util.List;

import lombok.Data;

@Data
public class Group {
	private String groupName;
	private List<Node> nodes;
}
