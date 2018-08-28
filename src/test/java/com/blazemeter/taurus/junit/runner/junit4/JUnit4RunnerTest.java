package com.blazemeter.taurus.junit.runner.junit4;

import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(TestCategory.class)
public class JUnit4RunnerTest extends TestCase {

    public void testGetClassLoader() {
        JUnit4Runner runner = new JUnit4Runner();
        assertEquals(ClassLoader.getSystemClassLoader(), runner.getClassLoader());
    }
}