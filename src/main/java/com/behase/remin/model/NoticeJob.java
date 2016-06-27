package com.behase.remin.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NoticeJob {
    private NoticeItem item;
    private List<ResultValue> resultValues;

    @Data
    @AllArgsConstructor
    public static class ResultValue {
        private String hostAndPort;
        private String value;
    }
}
