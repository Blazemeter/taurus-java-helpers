package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.CustomRunner;
import com.blazemeter.taurus.junit.api.JUnitRunner;
import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import com.blazemeter.taurus.junit.runner.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit.runner.junit5.JUnit5Runner;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.JUNIT_VERSION;

public class Worker extends Thread {
    private static final Logger log = Logger.getLogger(CustomRunner.class.getName());

    private final Properties props = new Properties();
    private final Reporter reporter;
    private final ThreadCounter counter;

    private long iterations;
    private long startDelay;

    private JUnitRunner runner;
    private Object request;

    private boolean isStopped = false;

    public Worker(Properties properties,
                  Reporter reporter, ThreadCounter counter,
                  long startDelay, long iterations) {
        this.props.putAll(properties);
        this.reporter = reporter;
        this.counter = counter;

        this.startDelay = startDelay * 1000;
        this.iterations = iterations;

        initJUnit();
    }

    protected void initJUnit() {
        runner = getJUnitRunner(props.getProperty(JUNIT_VERSION));
        request = runner.createRequest(this.props);
    }

    @Override
    public void run() {
        makeDelay();
        try {
            counter.incrementActiveThreads();
            int iter = 0;
            while (true) {
                iter++;
                runner.executeRequest(request, reporter, counter);
                if (isStopped()) {
                    log.info(String.format("[%s] was stopped", getName()));
                    break;
                }

                if (iter >= iterations) {
                    log.info(String.format("[%s] Iteration limit reached, stopping", getName()));
                    isStopped = true;
                    break;
                }
            }
        } finally {
            counter.decrementActiveThreads();
        }
    }

    protected void makeDelay() {
        log.fine(String.format("[%s] start delay %s", getName(), startDelay));
        if (startDelay > 0) {
            try {
                sleep(startDelay);
            } catch (InterruptedException e) {
                log.log(Level.INFO, "Worker was interrupted", e);
                throw new CustomRunnerException("Worker was interrupted", e);
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

    public synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stopWorker() {
        this.isStopped = true;
    }
}
