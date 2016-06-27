package com.behase.remin.model;

import lombok.Data;

@Data
public class ErrorResponse {
    private Error error;

    public ErrorResponse(String code, String message) {
        this.error = new Error();
        this.error.code = code;
        this.error.message = message;
    }

    @Data
    public static class Error {
        private String code;
        private String message;
    }
}
