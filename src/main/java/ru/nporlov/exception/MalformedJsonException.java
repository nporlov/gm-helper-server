package ru.nporlov.exception;

public class MalformedJsonException extends RuntimeException {
    public MalformedJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
