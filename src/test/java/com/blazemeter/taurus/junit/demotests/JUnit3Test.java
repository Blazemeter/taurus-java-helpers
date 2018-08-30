package com.blazemeter.taurus.junit.demotests;

import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(TestCategory.class)
public class JUnit3Test extends TestCase {
    public void testJUnit3Method() {
        assertEquals("1", "1");
    }
}
