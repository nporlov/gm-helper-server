package ru.nporlov.dto;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    private int status;
    private String code;
    private String message;
    private Instant timestamp;
    private Map<String, String> details;

    public ApiError(int  status, String code, String message, Instant timestamp, Map<String, String> details) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }
}
