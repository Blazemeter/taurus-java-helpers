package com.blazemeter.taurus.junit.reporting;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.*;

public class TaurusReporterTest {

    @Test
    public void testCSV() throws Exception {
        File file = File.createTempFile("report", ".csv");
        file.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(file.getAbsolutePath());
        reporter.incrementActiveThreads();
        reporter.incrementActiveThreads();
        reporter.incrementActiveThreads();
        reporter.incrementActiveThreads();

        assertFalse(reporter.isVerbose());

        long t1 = System.currentTimeMillis();
        Sample sample = new Sample();
        long t2 = System.currentTimeMillis();
        long startTime = sample.getStartTime();
        assertTrue(startTime >= t1);
        assertTrue(startTime <= t2);

        long duration = t2 + 5000 - startTime;
        sample.setDuration(duration);

        sample.setLabel("TestLabel");
        sample.setErrorMessage("Oppps!");
        sample.setStatus(Sample.STATUS_FAILED);

        reporter.writeSample(sample);
        reporter.close();

        StringBuilder expect = new StringBuilder("timeStamp,elapsed,Latency,label,responseCode,responseMessage,success,allThreads,bytes\n");
        expect.append(startTime).append(',');
        expect.append(duration).append(',');
        expect.append("0,TestLabel,,Oppps!,false,4,");
        expect.append("Oppps!".getBytes().length).append("\r\n");

        String actual = readFileToString(file);
        assertEquals(expect.toString(), actual);
    }

    @Test
    public void testLDJSON() throws Exception {
        File file = File.createTempFile("report", ".ldjson");
        file.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(file.getAbsolutePath());

        assertTrue(reporter.isVerbose());

        Sample sample = new Sample();
        long startTime = sample.getStartTime();
        sample.setDuration(5000);
        sample.setLabel("TestLabel");
        sample.setErrorMessage("Oppps!");
        sample.setStatus(Sample.STATUS_FAILED);

        reporter.writeSample(sample);
        reporter.close();

        StringBuilder expect = new StringBuilder("{\"duration\":5,\"start_time\":");
        expect.append(startTime / 1000);
        expect.append(",\"test_suite\":\"\",\"error_msg\":\"Oppps!\",\"extras\":{\"full_name\":\"\"},\"error_trace\":\"\",\"test_case\":\"TestLabel\",\"status\":\"FAILED\"}\n");

        String actual = readFileToString(file);
        assertEquals(expect.toString(), actual);
    }

    @Test
    public void testActiveThreads() throws Exception {
        File file = File.createTempFile("report", ".ldjson");
        file.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(file.getAbsolutePath());
        reporter.incrementActiveThreads();

        assertEquals(1, reporter.getActiveThreads());

        reporter.decrementActiveThreads();
        assertEquals(0, reporter.getActiveThreads());
    }

    @Test
    public void testBadFilename() {
        try {
            new TaurusReporter("/");
            fail();
        } catch (RuntimeException ex) {
            assertEquals("Failed to open file /", ex.getMessage());
        }
    }

    @Test
    public void testFilePermissions() throws Exception {
        final File file = File.createTempFile("test", ".csv");
        file.deleteOnExit();
        try {
            TaurusReporter reporter = new TaurusReporter(file.getAbsolutePath()) {
                @Override
                protected FileWriter openFile(String fileName) {
                    try {
                        return new FileWriter(file) {
                            @Override
                            public void write(String str) throws IOException {
                                throw new IOException("Opps");
                            }
                        };
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            fail();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            assertEquals("Failed to write CSV header", ex.getMessage());
        }
    }
}