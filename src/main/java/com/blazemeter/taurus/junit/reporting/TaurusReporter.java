package com.blazemeter.taurus.junit.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.blazemeter.taurus.junit.Utils;
import org.json.*;

public class TaurusReporter {

    private FileWriter outStream;
    private static final Logger log = Logger.getLogger(TaurusReporter.class.getName());
    private final LinkedBlockingQueue<Sample> queue = new LinkedBlockingQueue<>();

    private volatile boolean isStopped = false;
    private final SampleFormatter formatter;
    private final Thread reporter;

    public TaurusReporter(String fileName) {
        File file = new File(fileName);
        try {
            outStream = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file " + fileName, e);
        }

        reporter = new PoolWorker();
        reporter.setName("Reporter thread");
        reporter.setDaemon(true);
        reporter.start();

        formatter = createFormatter(file);
        log.info("File: " + fileName + ", formatter: " + formatter.getClass().getSimpleName());
    }

    private SampleFormatter createFormatter(File file) {
        return (Utils.getFileExtension(file).equals("csv")) ?
                new CSVFormatter() :
                new JSONFormatter();
    }

    public void writeSample(Sample sample) {
        try {
            queue.put(sample);
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, "Failed put sample in queue: ", e);
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
            while (!isStopped) {
                Sample sample = queue.poll();
                try {
                    outStream.write(formatter.formatToString(sample));
                    outStream.flush();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Failed write sample: ", e);
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
            obj.put("start_time", sample.getStartTime());
            obj.put("duration", sample.getDuration());
            obj.put("status", sample.getStatus());
            obj.put("error_msg", sample.getErrorMessage());
            obj.put("error_trace", sample.getErrorTrace());
            JSONObject extras = new JSONObject();
            extras.put("full_name", sample.getFullName());
            obj.put("extras", extras);
            return obj.toString() + "\n";
        }
    }

    private static class CSVFormatter implements SampleFormatter {
        @Override
        public String formatToString(Sample sample) {
            return null;
        }
    }
}
