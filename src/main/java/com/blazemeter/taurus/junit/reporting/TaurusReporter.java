package com.blazemeter.taurus.junit.reporting;

import com.blazemeter.taurus.junit.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaurusReporter {

    private FileWriter outStream;
    private static final Logger log = Logger.getLogger(TaurusReporter.class.getName());
    private final LinkedBlockingQueue<Sample> queue = new LinkedBlockingQueue<>();

    private int activeThreads = 0;

    private volatile boolean isStopped = false;
    private final SampleFormatter formatter;
    private final Thread reporter;

    private final boolean isVerbose;

    public TaurusReporter(String fileName) {
        outStream = openFile(fileName);
        formatter = createFormatter(fileName);
        isVerbose = formatter instanceof JSONFormatter;

        log.info("File: " + fileName + ", formatter: " + formatter.getClass().getSimpleName());

        reporter = new PoolWorker();
        reporter.setName("Reporter thread");
        reporter.setDaemon(true);
        reporter.start();
    }

    protected FileWriter openFile(String fileName) {
        try {
            return new FileWriter(new File(fileName));
        } catch (IOException e) {
            isStopped = true;
            throw new RuntimeException("Failed to open file " + fileName, e);
        }
    }

    protected SampleFormatter createFormatter(String fileName) {
        if (fileName.endsWith(".ldjson")) {
            return new JSONFormatter();
        } else {
            CSVFormatter formatter = new CSVFormatter();
            try {
                outStream.write(formatter.getHeader());
                outStream.write("\n");
            } catch (IOException e) {
                isStopped = true;
                log.log(Level.SEVERE, "Failed to write CSV header", e);
                throw new RuntimeException("Failed to write CSV header", e);
            }
            return formatter;
        }
    }

    public void writeSample(Sample sample) {
        try {
            queue.put(sample);
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, "Failed to put sample in queue: ", e);
        }
    }

    public void close() throws Exception {
        log.info("Closing reporter stream");
        isStopped = true;
        reporter.join();
        outStream.close();
    }

    private class PoolWorker extends Thread {
        public void run() {
            while (!isStopped || !queue.isEmpty()) {
                Sample sample;
                try {
                    sample = queue.poll(100, TimeUnit.MICROSECONDS);
                } catch (InterruptedException e) {
                    isStopped = true;
                    log.log(Level.SEVERE, "Reporter was interrupted", e);
                    throw new RuntimeException("Reporter was interrupted", e);
                }
                if (sample != null) {
                    try {
                        outStream.write(formatter.formatToString(sample));
                        outStream.flush();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Failed to write sample: ", e);
                    }
                }
            }
        }
    }

    private interface SampleFormatter {
        String formatToString(Sample sample);
    }

    private static class JSONFormatter implements SampleFormatter {
        @Override
        public String formatToString(Sample sample) {
            JSONObject obj = new JSONObject();
            obj.put("test_case", sample.getLabel());
            obj.put("test_suite", sample.getSuite());
            obj.put("start_time", sample.getStartTimeInSec());
            obj.put("duration", sample.getDurationInSec());
            obj.put("status", sample.getStatus());
            obj.put("error_msg", sample.getErrorMessage());
            obj.put("error_trace", sample.getErrorTrace());
            JSONObject extras = new JSONObject();
            extras.put("full_name", sample.getFullName());
            obj.put("extras", extras);
            return obj.toString() + "\n";
        }
    }

    private class CSVFormatter implements SampleFormatter {
        private final String CSV_HEADER = "timeStamp,elapsed,Latency,label,responseCode,responseMessage,success,allThreads,bytes";

        public String getHeader() {
            return CSV_HEADER;
        }

        @Override
        public String formatToString(Sample sample) {
            final StringBuilder builder = new StringBuilder();
            builder.append(sample.getStartTime()).append(','); // timeStamp in ms
            builder.append(sample.getDuration()).append(','); //elapsed in ms
            builder.append("0,"); //Latency
            builder.append(sample.getLabel()).append(','); // label

            builder.append(','); // responseCode
            builder.append(sample.getErrorMessage()).append(','); // responseMessage

            builder.append(sample.isSuccessful()).append(','); // success
            builder.append(getActiveThreads()).append(","); // allThreads
            builder.append(sample.getErrorMessage().getBytes().length).append("\r\n");
            return builder.toString();
        }
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public synchronized void incrementActiveThreads() {
        activeThreads++;
    }

    public synchronized void decrementActiveThreads() {
        activeThreads--;
    }

    public synchronized int getActiveThreads() {
        return activeThreads;
    }

    public boolean isStopped() {
        return isStopped;
    }
}
