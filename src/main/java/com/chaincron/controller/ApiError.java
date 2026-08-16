package com.chaincron.controller;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class ApiError {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }

    public static ApiError of(int status, String error, String message, String path) {
        return ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ApiError withFields(int status, String error, String message, String path, List<FieldError> fields) {
        return ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(fields)
                .build();
    }
}
