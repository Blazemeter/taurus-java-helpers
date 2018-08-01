package com.blazemeter.taurus.junit.runner.junit4;

import com.blazemeter.taurus.junit.demotests.EmptyTestClass;
import com.blazemeter.taurus.junit.demotests.JUnit3Test;
import com.blazemeter.taurus.junit.demotests.JUnit4Test;
import com.blazemeter.taurus.junit.demotests.JUnit5Test;
import com.blazemeter.taurus.junit.demotests.NoTestClass;
import junit.framework.TestCase;


public class JUnit4ClassFilterTest extends TestCase {

    public void testFlow() {
        JUnit4ClassFilter filter = new JUnit4ClassFilter();
        assertTrue(filter.shouldAdd(EmptyTestClass.class));
        assertTrue(filter.shouldAdd(JUnit3Test.class));
        assertTrue(filter.shouldAdd(JUnit4Test.class));
        assertFalse(filter.shouldAdd(JUnit5Test.class));
        assertFalse(filter.shouldAdd(NoTestClass.class));
    }
}