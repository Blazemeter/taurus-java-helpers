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

public class Supervisor {
    private static final Logger log = Logger.getLogger(Supervisor.class.getName());

    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());

    private final List<Class> classes;
    private final Properties properties;
    private final TaurusReporter reporter;

    private int concurrency;
    private int steps;
    private float rampUp;

    public Supervisor(List<Class> classes, Properties properties) {
        this.properties = properties;
        this.classes = classes;
        this.reporter = new TaurusReporter(properties.getProperty(REPORT_FILE));
        initParams(properties);
    }

    private void initParams(Properties props) {
        concurrency = Integer.valueOf(props.getProperty(CONCURRENCY, "1"));
        steps = Integer.valueOf(props.getProperty(STEPS, "1"));
        rampUp = Float.valueOf(props.getProperty(RAMP_UP, "0"));
    }

    public void run() {
        for (int i = 0; i < concurrency; i++) {
            Worker worker = new Worker(classes, properties, reporter, getWorkerDelay(i));
            worker.setName("Worker #" +i);
            worker.start();
            workers.add(worker);
        }

        workers.forEach(this::waitThreadStopped);
        closeReporter();
    }

    private long getWorkerDelay(int i) {
        float stepGranularity = rampUp / (float) steps;
        float ramp_up_per_thread = rampUp / (float) concurrency;
        float offset = i * ramp_up_per_thread / (float) concurrency;

        float delay = offset + i * rampUp / (float) concurrency;
        if (!(stepGranularity > 0)) {
            stepGranularity = 0;
        }
        delay -= delay % stepGranularity;
        return (long) delay;
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


    private void closeReporter() {
        try {
            reporter.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed close reporter", e);
        }
    }
}
