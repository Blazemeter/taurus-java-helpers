package com.blazemeter.taurus.junit.demotests;

import categories.TestCategory;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(TestCategory.class)
public class JUnit4Test {
    @Test
    public void testJUnit4Method() {
        assertEquals("1", "1");
    }
}
