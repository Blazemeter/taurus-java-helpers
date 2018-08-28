package com.blazemeter.taurus.junit.demotests;

import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(TestCategory.class)
public class EmptyTestClass extends TestCase {

    public void testFlow() {
        assertEquals(1, 1);
    }
}
