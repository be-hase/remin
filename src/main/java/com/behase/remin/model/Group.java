package com.behase.remin.model;

import lombok.Data;

import java.util.List;

@Data
public class Group {
    private String groupName;
    private List<Node> nodes;
}
