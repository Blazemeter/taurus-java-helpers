package com.blazemeter.taurus.junit;

import com.blazemeter.taurus.classpath.Filter;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

public class ClassFilter implements Filter {
    private static final Logger log = Logger.getLogger(ClassFilter.class.getName());

    @Override
    public boolean shouldAdd(Class c) {
        log.info("TestCase.class.isAssignableFrom(" + c.getCanonicalName() + ") = " + TestCase.class.isAssignableFrom(c));
        log.info("hasAnnotations(" + c.getCanonicalName() + ") = " + hasAnnotations(c));

        if (Modifier.isAbstract(c.getModifiers())) {
            log.info("Skip because of abstract");
            return false;
        }

        if (TestCase.class.isAssignableFrom(c) || hasAnnotations(c)) { // TODO : JUNIT 5 classes??
            log.info("class added to tests: " + c.getCanonicalName());
            return true;
        }

        return false;
    }

    protected boolean hasAnnotations(Class<?> c) {
        for (Method method : c.getMethods()) {
            if (method.isAnnotationPresent(org.junit.Test.class)) {
                return true;
            }
        }

        return false;
    }
}
