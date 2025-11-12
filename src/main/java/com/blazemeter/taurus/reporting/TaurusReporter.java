package com.blazemeter.taurus.reporting;

import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import com.blazemeter.taurus.utils.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaurusReporter implements Reporter {

    private FileWriter reportOutStream;
    private FileWriter errorOutStream;
    private static final Logger log = Logger.getLogger(TaurusReporter.class.getName());
    private final LinkedBlockingQueue<Sample> queue = new LinkedBlockingQueue<>();

    private volatile boolean isStopped = false;
    private final SampleFormatter formatter;
    private final Thread reporter;

    private final boolean isVerbose;

    public TaurusReporter(String reportFileName) throws IOException {
        this(reportFileName, null);
    }

    public TaurusReporter(String reportFileName, String errorFileName) throws IOException {
        reportOutStream = openFile(reportFileName);
        if (errorFileName != null) {
            errorOutStream = openFile(errorFileName);
        }
        formatter = createFormatter(reportFileName);
        isVerbose = formatter instanceof JSONFormatter;

        log.info("File: " + reportFileName + ", formatter: " + formatter.getClass().getSimpleName());

        reporter = new PoolWorker();
        reporter.setName("Reporter thread");
        reporter.setDaemon(true);
        reporter.start();
    }

    protected FileWriter openFile(String fileName) throws IOException {
        try {
            return new FileWriter(new File(fileName));
        } catch (IOException e) {
            isStopped = true;
            throw new IOException("Failed to open file " + fileName, e);
        }
    }

    protected SampleFormatter createFormatter(String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".ldjson")) {
            return new JSONFormatter();
        } else {
            CSVFormatter formatter = new CSVFormatter();
            try {
                reportOutStream.write(formatter.getHeader());
                reportOutStream.write("\n");
            } catch (IOException e) {
                isStopped = true;
                log.log(Level.SEVERE, "Failed to write CSV header", e);
                throw new IOException("Failed to write CSV header", e);
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
        reportOutStream.close();
        if (errorOutStream != null) {
            errorOutStream.close();
        }
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
                    throw new CustomRunnerException("Reporter was interrupted", e);
                }
                if (sample != null) {
                    try {
                        reportOutStream.write(formatter.formatToString(sample));
                        reportOutStream.flush();
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
            // using JSONObject.wrap to keep null values in resulting JSON
            obj.put("error_msg", JSONObject.wrap(sample.getErrorMessage()));
            obj.put("error_trace", JSONObject.wrap(sample.getErrorTrace()));
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
            String errorMsg = sample.getErrorMessage() == null ? "" : sample.getErrorMessage();
            String sanitizedTrace = sample.getErrorTrace() == null ? "" : sample.getErrorTrace();
            String combinedErrorMsg = Utils.escapeCSV(errorMsg + (sanitizedTrace.isEmpty() ? "" : " | " + sanitizedTrace));
            builder.append(combinedErrorMsg).append(','); // responseMessage ( + stack trace)

            builder.append(sample.isSuccessful()).append(','); // success
            builder.append(sample.getActiveThreads()).append(","); // allThreads
            builder.append(errorMsg.getBytes().length).append("\r\n");
            return builder.toString();
        }

        private String formatMessage(String errorMessage) {
            return Utils.escapeCSV(errorMessage);
        }
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public boolean isStopped() {
        return isStopped;
    }
}
