package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import com.blazemeter.taurus.reporting.TaurusReporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.CONCURRENCY;
import static com.blazemeter.taurus.junit.CustomRunner.HOLD;
import static com.blazemeter.taurus.junit.CustomRunner.ITERATIONS;
import static com.blazemeter.taurus.junit.CustomRunner.RAMP_UP;
import static com.blazemeter.taurus.junit.CustomRunner.REPORT_FILE;
import static com.blazemeter.taurus.junit.CustomRunner.STEPS;

public class Supervisor {
    private static final Logger log = Logger.getLogger(Supervisor.class.getName());

    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());

    protected final Properties properties;
    protected final Reporter reporter;
    protected final ThreadCounter counter;

    private int concurrency;
    private int steps;
    protected long iterations;
    private float rampUp;
    private float hold;

    private volatile boolean isInterrupted = false;
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    public Supervisor(Properties properties) {
        this.properties = properties;
        try {
            this.reporter = new TaurusReporter(properties.getProperty(REPORT_FILE));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to create reporter", e);
            throw new CustomRunnerException("Failed to create reporter", e);
        }
        this.counter = new Counter();
        initParams(properties);
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                isInterrupted = true;
                stopWorkers();
            }
        });
    }


    private void initParams(Properties props) {
        concurrency = Integer.valueOf(props.getProperty(CONCURRENCY, "1"));
        steps = Integer.valueOf(props.getProperty(STEPS, String.valueOf(Integer.MAX_VALUE)));
        rampUp = Float.valueOf(props.getProperty(RAMP_UP, "0"));
        hold = Float.valueOf(props.getProperty(HOLD, "0"));
        iterations = Long.valueOf(props.getProperty(ITERATIONS, "0"));
        if (iterations == 0) {
            if (hold > 0) {
                iterations = Long.MAX_VALUE;
            } else {
                iterations = 1;
            }
        }
    }

    protected void createWorkers() {
        for (int i = 0; i < concurrency; i++) {
            Worker worker = createWorker(i);
            worker.setName("Worker #" + i);
            worker.setDaemon(false);
            workers.add(worker);
        }
    }

    protected Worker createWorker(int workerId) {
        return new Worker(properties, reporter, counter, getWorkerDelay(workerId), iterations);
    }

    public void execute() {
        createWorkers();
        long workingTime = (long) (rampUp + hold) * 1000;
        long endTime = (workingTime == 0) ? 0 : (System.currentTimeMillis() + workingTime);
        for (Worker w : workers) {
            w.start();
        }
        waitForFinish(endTime);
    }

    private void waitForFinish(long endTime) {
        while (true) {
            if (isInterrupted) {
                log.fine("Supervisor was interrupted. Break loop.");
                stopWorkers();
                break;
            }

            long currTime = System.currentTimeMillis();
            if (0 < endTime && endTime <= currTime) {
                log.info("Duration limit reached, stopping");
                stopWorkers();
                break;
            }

            if (!isWorkersAlive()) {
                log.info("All workers finished, stopping");
                stopWorkers();
                break;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                isInterrupted = true;
                log.warning("Supervisor was interrupted");
            }
        }
    }

    private boolean isWorkersAlive() {
        for (Worker w : workers) {
            if (w.isAlive()) {
                return true;
            }
        }
        return false;
    }

    protected void stopWorkers() {
        if (isStopped.compareAndSet(false, true)) {
            for (Worker w : workers) {
                w.stopWorker();
            }
            for (Worker w : workers) {
                waitWorkerStopped(w);
            }
            closeReporter();
        }
    }

    private void waitWorkerStopped(Worker worker) {
        if (worker == null) {
            return;
        }

        while (worker.isAlive()) {
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Interrupted while wait for finish " + worker.getName());
            }
        }
    }

    protected long getWorkerDelay(int i) {
        float stepGranularity = rampUp / (float) steps;
        float ramp_up_per_thread = rampUp / (float) concurrency;
        float offset = i * ramp_up_per_thread / (float) concurrency;

        float delay = offset + i * rampUp / (float) concurrency;
        if (stepGranularity < 0) {
            stepGranularity = 0;
        }
        delay -= delay % stepGranularity;
        return (long) delay;
    }

    private void closeReporter() {
        try {
            reporter.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed close reporter", e);
        }
    }
}
