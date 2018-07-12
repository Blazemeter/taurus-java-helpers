package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.TaurusReporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.CONCURRENCY;
import static com.blazemeter.taurus.junit.CustomRunner.RAMP_UP;
import static com.blazemeter.taurus.junit.CustomRunner.REPORT_FILE;
import static com.blazemeter.taurus.junit.CustomRunner.STEPS;

public class Supervisor extends Thread {
    private static final Logger log = Logger.getLogger(Supervisor.class.getName());

    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());

    private final List<Class> classes;
    private final Properties properties;
    private final TaurusReporter reporter;

    private int concurrency;
    private int steps;
    private long rampUp;
//    private long iterations;
//    private float hold;
//    private long delay;

    private int totalConcurrency = 0;

    public Supervisor(List<Class> classes, Properties properties) {
        this.properties = properties;
        this.classes = classes;
        this.reporter = new TaurusReporter(properties.getProperty(REPORT_FILE));
        initParams(properties);
        setName(Supervisor.class.getName());
        setDaemon(true);
    }

    // todo: catch NumberFormatException
    // todo: types for time values?
    private void initParams(Properties props) {
        concurrency = Integer.valueOf(props.getProperty(CONCURRENCY, "0"));
        steps = Integer.valueOf(props.getProperty(STEPS, "1"));
        rampUp = Long.valueOf(props.getProperty(RAMP_UP, "0"));
    }

    @Override
    public void run() {
        int step = concurrency / steps;
        long stepDelay = rampUp / steps;

        for (int i = 0; i < steps; i++) {
            if (i == steps - 1) {
                step = concurrency - totalConcurrency;
            }
            executeWorkers(step);
            totalConcurrency += step;
            makeDelay(stepDelay);
        }

        workers.forEach(this::waitThreadStopped);
        closeReporter();
    }

    private void makeDelay(long delay) {
        if (delay > 0) {
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Supervisor was interrupted", e);
                throw new RuntimeException("Supervisor was interrupted", e);
            }
        }
    }

    private void waitThreadStopped(Thread thread) {
        if (thread == null) {
            return;
        }
        while (thread.isAlive()) {
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    private void executeWorkers(int workersCount) {
        for (int i = 0; i < workersCount; i++) {
            Worker worker = new Worker(classes, properties, reporter);
            worker.setName("Worker #" + (totalConcurrency + i));
            worker.start();
            workers.add(worker);
        }
    }

    private void closeReporter() {
        try {
            reporter.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed close reporter", e);
        }
    }
}
