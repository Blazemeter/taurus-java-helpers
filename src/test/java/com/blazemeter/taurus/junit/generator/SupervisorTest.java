package com.blazemeter.taurus.junit.generator;

import categories.TestCategory;
import com.blazemeter.taurus.junit.CustomRunner;
import com.blazemeter.taurus.junit.api.JUnitRunner;
import com.blazemeter.taurus.junit.TestJUnitRunner;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.blazemeter.taurus.junit.CustomRunnerTest.getLinesCount;

@Category(TestCategory.class)
public class SupervisorTest extends TestCase {

    public void testDelayCalculation() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(0));
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(10));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(14));
        props.setProperty(CustomRunner.STEPS, String.valueOf(3));

        Supervisor supervisor = new Supervisor(props);
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
        supervisor = new Supervisor(props);

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
        supervisor = new Supervisor(props);

        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(0, supervisor.getWorkerDelay(5));
        assertEquals(0, supervisor.getWorkerDelay(9));

        // new case
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(10));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(50));
        props.setProperty(CustomRunner.STEPS, String.valueOf(1));
        supervisor = new Supervisor(props);

        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(0, supervisor.getWorkerDelay(9));

        props.setProperty(CustomRunner.STEPS, "-10");
        supervisor = new Supervisor(props);
        assertEquals(0, supervisor.getWorkerDelay(4));
    }

    public void testDelayCalculationWithoutSteps() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(0));
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(5));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(10));

        Supervisor supervisor = new Supervisor(props);
        assertEquals(0, supervisor.getWorkerDelay(0));
        assertEquals(2, supervisor.getWorkerDelay(1));
        assertEquals(4, supervisor.getWorkerDelay(2));
        assertEquals(7, supervisor.getWorkerDelay(3));
        assertEquals(9, supervisor.getWorkerDelay(4));
    }

    public void testFailedToCreateReporter() throws IOException {
        File report = File.createTempFile("report", ".ldjson");
        report.setWritable(false);
        report.deleteOnExit();

        Properties properties = new Properties();
        properties.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());

        try {
            new Supervisor(properties);
            fail("failed to create reporter");
        } catch (CustomRunnerException e) {
            assertEquals("Failed to create reporter", e.getMessage());
        }
    }

    public void testHold() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));

        Supervisor supervisor = new Supervisor(props) {
            @Override
            protected Worker createWorker(int workerId) {
                return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations) {
                    @Override
                    protected JUnitRunner getJUnitRunner(String junitVersion) {
                        return new TestJUnitRunner();
                    }
                };

            }
        };

        supervisor.execute();
        assertTrue(2 < getLinesCount(report));
    }

    public void testHoldAndIterations() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));

        Supervisor supervisor = new Supervisor(props) {
            @Override
            protected Worker createWorker(int workerId) {
                return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations) {
                    @Override
                    protected JUnitRunner getJUnitRunner(String junitVersion) {
                        return new TestJUnitRunner();
                    }
                };

            }
        };

        supervisor.execute();
        assertEquals(1, getLinesCount(report));
    }

    public void testIterations() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(3));

        Supervisor supervisor = new Supervisor(props) {
            @Override
            protected Worker createWorker(int workerId) {
                return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations) {
                    @Override
                    protected JUnitRunner getJUnitRunner(String junitVersion) {
                        return new TestJUnitRunner();
                    }
                };

            }
        };

        supervisor.execute();
        assertEquals(3, getLinesCount(report));
    }

    public void testMain() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());

        Supervisor supervisor = new Supervisor(props) {
            @Override
            protected Worker createWorker(int workerId) {
                return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations) {
                    @Override
                    protected JUnitRunner getJUnitRunner(String junitVersion) {
                        return new TestJUnitRunner();
                    }
                };

            }
        };

        supervisor.execute();
        assertEquals(1, getLinesCount(report));
    }


    public void testConcurrency() throws Exception {
        File report = File.createTempFile("report", ".csv");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.CONCURRENCY, "10");
        props.setProperty(CustomRunner.ITERATIONS, "5");

        Supervisor supervisor = new Supervisor(props) {
            @Override
            protected Worker createWorker(int workerId) {
                return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations) {
                    @Override
                    protected JUnitRunner getJUnitRunner(String junitVersion) {
                        return new TestJUnitRunner();
                    }
                };

            }
        };

        supervisor.execute();
        assertEquals(51, getLinesCount(report)); // 5 * 10 - reports line + header
    }


    public void testRampUp() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(5));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(3));
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(3));

        Supervisor supervisor = new Supervisor(props) {
            @Override
            protected Worker createWorker(int workerId) {
                return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations) {
                    @Override
                    protected JUnitRunner getJUnitRunner(String junitVersion) {
                        return new TestJUnitRunner();
                    }
                };

            }
        };

        supervisor.execute();
        assertTrue(3 < getLinesCount(report));
    }
}