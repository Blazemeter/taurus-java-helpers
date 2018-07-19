package com.blazemeter.taurus.junit.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class CustomRunnerExceptionTest {

    @Test
    public void test() {
        CustomRunnerException exception = new CustomRunnerException();
        assertNotNull(exception);
        exception = new CustomRunnerException("msg");
        assertEquals("msg", exception.getMessage());
        exception = new CustomRunnerException("msg1", new RuntimeException("nested exception"));
        assertEquals("nested exception", exception.getCause().getMessage());
    }

}