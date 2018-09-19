package com.blazemeter.taurus.junit.runner.junit5;

import com.blazemeter.taurus.junit.runner.junit4.JUnit4ClassFilter;
import junit.framework.TestCase;

import java.util.logging.Logger;


public class JUnit5ClassFilter extends JUnit4ClassFilter {
    private static final Logger log = Logger.getLogger(JUnit5ClassFilter.class.getName());

    protected void log(String msg) {
        log.fine(msg);
    }

    protected boolean isTestClass(Class c) {
        return TestCase.class.isAssignableFrom(c)
                || hasAnnotations(c, org.junit.Test.class)
                || hasAnnotations(c, org.junit.jupiter.api.Test.class);
    }
}
