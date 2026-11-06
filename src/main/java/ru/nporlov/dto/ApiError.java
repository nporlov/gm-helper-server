package ru.nporlov.dto;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    private int status;
    private String code;
    private String message;
    private Instant timestamp;
    private Map<String, String> details;

    //EMPTY CONSTRUCTOR
    public ApiError() {}

    public ApiError(int  status, String code, String message, Instant timestamp, Map<String, String> details) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.details = details;
    }

    public String getCode() {
        return code;
    }
    public int getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
}
