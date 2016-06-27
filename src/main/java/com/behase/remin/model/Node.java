package com.behase.remin.model;

import lombok.Data;

@Data
public class Node {
    private String hostAndPort;
    private String password;
    private boolean connected;
}
