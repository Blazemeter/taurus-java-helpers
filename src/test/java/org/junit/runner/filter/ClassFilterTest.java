package org.junit.runner.filter;

import org.junit.Test;
import org.junit.runner.Description;

import static org.junit.Assert.*;

public class ClassFilterTest {

    @Test
    public void testFlow() {
        Description description = Description.createSuiteDescription(ClassFilterTest.class);
        ClassFilter filter = new ClassFilter(description);
        assertTrue(filter.shouldRun(description));
        assertFalse(filter.shouldRun(Description.createSuiteDescription(ClassFilter.class)));
        assertEquals(ClassFilterTest.class.getName(), filter.describe());
    }
}