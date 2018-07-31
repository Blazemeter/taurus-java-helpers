package com.blazemeter.taurus.junit4;

import junit.framework.TestCase;

public class JUnit4RunnerTest extends TestCase {

    public void testGetClassLoader() {
        JUnit4Runner runner = new JUnit4Runner();
        assertEquals(ClassLoader.getSystemClassLoader(), runner.getClassLoader());
    }
}