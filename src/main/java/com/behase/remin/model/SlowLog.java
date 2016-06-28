package com.behase.remin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlowLog {
    private long id;
    private String hostAndPort;
    private long timeStamp;
    private long executionTime;
    private List<String> args;
}
