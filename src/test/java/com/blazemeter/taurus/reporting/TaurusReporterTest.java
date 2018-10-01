package com.blazemeter.taurus.reporting;

import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import static org.apache.commons.io.FileUtils.readFileToString;

@Category(TestCategory.class)
public class TaurusReporterTest extends TestCase {

    public void testCSV() throws Exception {
        File file = File.createTempFile("report", ".csv");
        file.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(file.getAbsolutePath());

        assertFalse(reporter.isVerbose());

        long t1 = System.currentTimeMillis();
        Sample sample = new Sample();
        sample.setActiveThreads(4);
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
        assertTrue(reporter.isStopped());

        StringBuilder expect = new StringBuilder("timeStamp,elapsed,Latency,label,responseCode,responseMessage,success,allThreads,bytes\n");
        expect.append(startTime).append(',');
        expect.append(duration).append(',');
        expect.append("0,TestLabel,,Oppps!,false,4,");
        expect.append("Oppps!".getBytes().length).append("\r\n");

        String actual = readFileToString(file);
        assertEquals(expect.toString(), actual);
    }

    public void testLDJSON() throws Exception {
        File file = File.createTempFile("report", ".lDjsOn");
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
        expect.append(BigDecimal.valueOf(startTime,3));
        expect.append(",\"test_suite\":\"\",\"error_msg\":\"Oppps!\",\"extras\":{\"full_name\":\"\"},\"error_trace\":null,\"test_case\":\"TestLabel\",\"status\":\"FAILED\"}\n");

        String actual = readFileToString(file);
        assertEquals(expect.toString(), actual);
    }

    public void testBadFilename() {
        try {
            new TaurusReporter("/");
            fail();
        } catch (IOException ex) {
            assertEquals("Failed to open file /", ex.getMessage());
        }
    }

    public void testFilePermissions() throws Exception {
        final File file = File.createTempFile("test", ".csv");
        file.deleteOnExit();
        try {
            new TaurusReporter(file.getAbsolutePath()) {
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
        } catch (IOException ex) {
            ex.printStackTrace();
            assertEquals("Failed to write CSV header", ex.getMessage());
        }
    }
}