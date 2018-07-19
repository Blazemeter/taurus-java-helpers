package com.blazemeter.taurus.junit.exception;

public class CustomRunnerException extends RuntimeException {
    public CustomRunnerException() {
        super();
    }

    public CustomRunnerException(String message) {
        super(message);
    }

    public CustomRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
