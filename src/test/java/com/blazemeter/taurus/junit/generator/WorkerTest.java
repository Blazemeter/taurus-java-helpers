package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.JUnitRunner;
import com.blazemeter.taurus.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit5.JUnit5Runner;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.*;

public class WorkerTest {


    @Test
    public void testJUnitVersion() {
        Worker worker = new Worker(new ArrayList<>(), new Properties(), null, 8888, 9999);

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