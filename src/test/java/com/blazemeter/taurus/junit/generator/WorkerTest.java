package com.blazemeter.taurus.junit.generator;

import categories.TestCategory;
import com.blazemeter.taurus.junit.api.JUnitRunner;
import com.blazemeter.taurus.junit.runner.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit.runner.junit5.JUnit5Runner;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.util.Properties;


@Category(TestCategory.class)
public class WorkerTest extends TestCase {
    public void testJUnitVersion() {
        Worker worker = new Worker(new Properties(), null, null, 8888, 9999) {
            @Override
            protected void initJUnit() {
                // NOOP
            }
        };

        JUnitRunner jUnitRunner = worker.getJUnitRunner(null);

        assertTrue(jUnitRunner instanceof JUnit4Runner);
        jUnitRunner = worker.getJUnitRunner("");
        assertTrue(jUnitRunner instanceof JUnit4Runner);

        jUnitRunner = worker.getJUnitRunner("4");
        assertTrue(jUnitRunner instanceof JUnit4Runner);
        jUnitRunner = worker.getJUnitRunner("5");
        assertTrue(jUnitRunner instanceof JUnit5Runner);
    }
}