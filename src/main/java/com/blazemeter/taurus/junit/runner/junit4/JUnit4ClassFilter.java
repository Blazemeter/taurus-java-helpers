package com.blazemeter.taurus.junit.runner.junit4;

import com.blazemeter.taurus.classpath.Filter;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

public class JUnit4ClassFilter implements Filter {
    private static final Logger log = Logger.getLogger(JUnit4ClassFilter.class.getName());

    @Override
    public boolean shouldAdd(Class c) {
        log.info("Checking class " + c.getName());

        if (Modifier.isAbstract(c.getModifiers())) {
            log.info("Skip because of abstract");
            return false;
        }

        if (isTestClass(c)) {
            log.info(String.format("Class '%s' added to tests", c.getName()));
            return true;
        }

        return false;
    }

    protected boolean isTestClass(Class c) {
        return TestCase.class.isAssignableFrom(c) || hasAnnotations(c, org.junit.Test.class);
    }

    protected boolean hasAnnotations(Class c, Class annotationCls) {
        for (Method method : c.getMethods()) {
            if (method.isAnnotationPresent(annotationCls)) {
                return true;
            }
        }

        return false;
    }
}
