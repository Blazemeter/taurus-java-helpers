package com.blazemeter.taurus.junit.runner.junit4;

import categories.TestCategory;
import com.blazemeter.taurus.junit.demotests.EmptyTestClass;
import com.blazemeter.taurus.junit.demotests.JUnit3Test;
import com.blazemeter.taurus.junit.demotests.JUnit4Test;
import com.blazemeter.taurus.junit.demotests.JUnit5Test;
import com.blazemeter.taurus.junit.demotests.NoTestClass;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Category;


@Category(TestCategory.class)
public class JUnit4ClassFilterTest extends TestCase {

    public void testFlow() {
        JUnit4ClassFilter filter = new JUnit4ClassFilter();
        assertFalse(filter.shouldAdd(TestSuite.class));
        assertTrue(filter.shouldAdd(EmptyTestClass.class));
        assertTrue(filter.shouldAdd(JUnit3Test.class));
        assertTrue(filter.shouldAdd(JUnit4Test.class));
        assertFalse(filter.shouldAdd(JUnit5Test.class));
        assertFalse(filter.shouldAdd(NoTestClass.class));
    }

    public void testExclude() throws ClassNotFoundException {
        JUnit4ClassFilter filter = new JUnit4ClassFilter();
        assertTrue(filter.isExcludedClass(Class.forName("junit.framework.TestSuite$1")));
    }
}