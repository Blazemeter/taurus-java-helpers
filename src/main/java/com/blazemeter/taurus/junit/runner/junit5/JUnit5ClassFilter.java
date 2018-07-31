package com.blazemeter.taurus.junit.runner.junit5;

import com.blazemeter.taurus.junit.runner.junit4.JUnit4ClassFilter;
import junit.framework.TestCase;


public class JUnit5ClassFilter extends JUnit4ClassFilter {

    protected boolean isTestClass(Class c) {
        return TestCase.class.isAssignableFrom(c)
                || hasAnnotations(c, org.junit.Test.class)
                || hasAnnotations(c, org.junit.jupiter.api.Test.class);
    }
}
