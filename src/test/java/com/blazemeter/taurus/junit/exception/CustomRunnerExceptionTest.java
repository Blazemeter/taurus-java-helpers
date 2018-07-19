package com.blazemeter.taurus.junit.exception;

import junit.framework.TestCase;

public class CustomRunnerExceptionTest extends TestCase {

    public void test() {
        CustomRunnerException exception = new CustomRunnerException();
        assertNotNull(exception);
        exception = new CustomRunnerException("msg");
        assertEquals("msg", exception.getMessage());
        exception = new CustomRunnerException("msg1", new RuntimeException("nested exception"));
        assertEquals("nested exception", exception.getCause().getMessage());
    }

}