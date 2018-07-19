package org.junit.runner.filter;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrFilterTest {

    @Test
    public void testFlow() {
        Description description1 = Description.createSuiteDescription(ClassFilterTest.class);
        ClassFilter classFilter1 = new ClassFilter(description1);

        Description description2 = Description.createSuiteDescription(ClassFilter.class);
        ClassFilter classFilter2 = new ClassFilter(description2);

        List<Filter> filters = new ArrayList<>();
        filters.add(classFilter1);
        filters.add(classFilter2);

        OrFilter filter = new OrFilter(filters);

        assertTrue(filter.shouldRun(description1));
        assertTrue(filter.shouldRun(description2));
        assertFalse(filter.shouldRun(Description.createSuiteDescription(OrFilterTest.class)));

        assertEquals(ClassFilterTest.class.getName() + " or " + ClassFilter.class.getName() + " ",
                filter.describe());
    }
}