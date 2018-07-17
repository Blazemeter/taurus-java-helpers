package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.CustomRunner;
import com.blazemeter.taurus.junit.JUnitRunner;
import com.blazemeter.taurus.junit.Reporter;
import com.blazemeter.taurus.junit.reporting.TaurusReporter;
import com.blazemeter.taurus.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit5.JUnit5Runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.*;

public class Worker extends Thread {
    private static final Logger log = Logger.getLogger(CustomRunner.class.getName());

    private final Properties props = new Properties();
    private final List<Class> classes = new ArrayList<>();
    private final TaurusReporter reporter;

    private long iterations;
    private long workingTime;
    private long startDelay;

    private JUnitRunner runner;
    private Object request;

    public Worker(List<Class> classes, Properties properties, TaurusReporter reporter, long startDelay) {
        this.props.putAll(properties);
        this.classes.addAll(classes);
        this.reporter = reporter;

        this.startDelay = startDelay * 1000;
        float rampUp = Float.valueOf(props.getProperty(RAMP_UP, "0"));
        float hold = Float.valueOf(props.getProperty(HOLD, "0"));
        workingTime = (long) (rampUp + hold) * 1000;

        iterations = Long.valueOf(props.getProperty(ITERATIONS, "0"));
        if (iterations == 0) {
            if (hold > 0) {
                iterations = Long.MAX_VALUE;
            } else {
                iterations = 1;
            }
        }
        runner = getJUnitRunner(props.getProperty(JUNIT_VERSION));
        request = runner.createRequest(classes, props);
    }

    @Override
    public void run() {
        long endTime = (workingTime == 0) ? 0 : (System.currentTimeMillis() + workingTime);
        makeDelay();
        try {
            reporter.incrementActiveThreads();

            int iter = 0;
            while (true) {
                iter++;
                runner.executeRequest(request, reporter);
                long currTime = System.currentTimeMillis();
                if (0 < endTime && endTime <= currTime) {
                    log.info(String.format("[%s] Duration limit reached, stopping", getName()));
                    break;
                }

                if (iter >= iterations) {
                    log.info(String.format("[%s] Iteration limit reached, stopping", getName()));
                    break;
                }
            }
        } finally {
            reporter.decrementActiveThreads();
        }
    }

    protected void makeDelay() {
        if (startDelay > 0) {
            try {
                sleep(startDelay);
            } catch (InterruptedException e) {
                log.log(Level.INFO, "Worker was interrupted", e);
                throw new RuntimeException("Worker was interrupted", e);
            }
        }
    }

    protected JUnitRunner getJUnitRunner(String junitVersion) {
        log.fine("Set JUnit version = " + junitVersion);
        if ("5".equals(junitVersion)) {
            return new JUnit5Runner();
        } else {
            return new JUnit4Runner();
        }
    }
}
