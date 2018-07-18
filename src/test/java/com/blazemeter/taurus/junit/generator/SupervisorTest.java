package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.CustomRunner;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.*;

public class SupervisorTest {

    @Test
    public void testDelayCalculation() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(0));
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(10));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(14));
        props.setProperty(CustomRunner.STEPS, String.valueOf(3));

        Supervisor supervisor = new Supervisor(new ArrayList<>(), props);
        // step 1
        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(0, supervisor.getWorkerDelay(3));
        // step 2
        assertEquals(4, supervisor.getWorkerDelay(4));
        assertEquals(4, supervisor.getWorkerDelay(6));
        // step 3
        assertEquals(9, supervisor.getWorkerDelay(7));
        assertEquals(9, supervisor.getWorkerDelay(9));

        // new case
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(400));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(200));
        props.setProperty(CustomRunner.STEPS, String.valueOf(4));
        supervisor = new Supervisor(new ArrayList<>(), props);

        // step 1
        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(0, supervisor.getWorkerDelay(99));
        // step 2
        assertEquals(50, supervisor.getWorkerDelay(100));
        assertEquals(50, supervisor.getWorkerDelay(199));
        // step 3
        assertEquals(100, supervisor.getWorkerDelay(200));
        assertEquals(100, supervisor.getWorkerDelay(299));
        // step 4
        assertEquals(150, supervisor.getWorkerDelay(300));
        assertEquals(150, supervisor.getWorkerDelay(399));

        // new case
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(10));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(0));
        props.setProperty(CustomRunner.STEPS, String.valueOf(4));
        supervisor = new Supervisor(new ArrayList<>(), props);

        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(0, supervisor.getWorkerDelay(5));
        assertEquals(0, supervisor.getWorkerDelay(9));

        // new case
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(10));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(50));
        props.setProperty(CustomRunner.STEPS, String.valueOf(1));
        supervisor = new Supervisor(new ArrayList<>(), props);

        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(0, supervisor.getWorkerDelay(9));
    }

    @Test
    public void testDelayCalculationWithoutSteps() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(0));
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(5));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(10));

        Supervisor supervisor = new Supervisor(new ArrayList<>(), props);
        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(2, supervisor.getWorkerDelay(1));
        assertEquals(4, supervisor.getWorkerDelay(2));
        assertEquals(7, supervisor.getWorkerDelay(3));
        assertEquals(9, supervisor.getWorkerDelay(4));
    }
}