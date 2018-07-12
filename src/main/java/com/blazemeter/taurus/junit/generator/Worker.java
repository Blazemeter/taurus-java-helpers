package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.CustomRunner;
import com.blazemeter.taurus.junit.JUnitRunner;
import com.blazemeter.taurus.junit.TaurusReporter;
import com.blazemeter.taurus.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit5.JUnit5Runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.DELAY;
import static com.blazemeter.taurus.junit.CustomRunner.HOLD;
import static com.blazemeter.taurus.junit.CustomRunner.ITERATIONS;
import static com.blazemeter.taurus.junit.CustomRunner.JUNIT_VERSION;

public class Worker extends Thread {
    private static final Logger log = Logger.getLogger(CustomRunner.class.getName());

    private final Properties props = new Properties();
    private final List<Class> classes = new ArrayList<>();
    private final TaurusReporter reporter;

    private long iterations;
    private float hold;
    private long delay;

    public Worker(List<Class> classes, Properties properties, TaurusReporter reporter) {
        this.props.putAll(properties);
        this.classes.addAll(classes);
        this.reporter = reporter;

        delay = Long.valueOf(props.getProperty(DELAY, "0"));
        iterations = Long.valueOf(props.getProperty(ITERATIONS, "0"));
        hold = Float.valueOf(props.getProperty(HOLD, "0"));
        if (iterations == 0) {
            if (hold > 0) {
                iterations = Long.MAX_VALUE;
            } else {
                iterations = 1;
            }
        }
    }

    @Override
    public void run() {
        JUnitRunner runner = getJUnitRunner(props.getProperty(JUNIT_VERSION));
        Object request = runner.createRequest(classes, props);

        long startTime = System.currentTimeMillis();
        for (int iteration = 0; iteration < iterations; iteration++) {
            makeDelay(); // todo: here???
            runner.executeRequest(request, reporter);
            log.info("Elapsed: " + (System.currentTimeMillis() - startTime) + ", limit: " + (hold * 1000));
            if (hold > 0 && System.currentTimeMillis() - startTime > hold * 1000) {
                log.info("Duration limit reached, stopping");
                break;
            }
        }
    }

    protected void makeDelay() {
        if (delay > 0) {
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Worker was interrupted", e);
                throw new RuntimeException("Worker was interrupted", e);
            }
        }
    }

    protected static JUnitRunner getJUnitRunner(String junitVersion) {
        log.fine("Set JUnit version = " + junitVersion);
        if (junitVersion == null || junitVersion.isEmpty() || junitVersion.equals("4")) {
            log.fine("Will use JUnit 4 version");
            return new JUnit4Runner();
        } else if (junitVersion.equals("5")) {
            log.fine("Will use JUnit 5 version");
            return new JUnit5Runner();
        } else {
            log.warning("Cannot detect JUnit version=" + junitVersion + ". Will use JUnit 4 version");
            return new JUnit4Runner();
        }
    }
}
