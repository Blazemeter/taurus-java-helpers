package com.blazemeter.taurus.junit.runner.junit5;

import categories.TestCategory;
import com.blazemeter.taurus.junit.demotests.EmptyTestClass;
import com.blazemeter.taurus.junit.demotests.JUnit3Test;
import com.blazemeter.taurus.junit.demotests.JUnit4Test;
import com.blazemeter.taurus.junit.demotests.JUnit5Test;
import com.blazemeter.taurus.junit.demotests.NoTestClass;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(TestCategory.class)
public class JUnit5ClassFilterTest extends TestCase {

    public void testFlow() {
        JUnit5ClassFilter filter = new JUnit5ClassFilter();
        assertTrue(filter.shouldAdd(EmptyTestClass.class));
        assertTrue(filter.shouldAdd(JUnit3Test.class));
        assertTrue(filter.shouldAdd(JUnit4Test.class));
        assertTrue(filter.shouldAdd(JUnit5Test.class));
        assertFalse(filter.shouldAdd(NoTestClass.class));
    }
}