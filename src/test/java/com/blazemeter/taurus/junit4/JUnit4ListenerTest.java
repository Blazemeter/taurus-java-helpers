package com.blazemeter.taurus.junit4;

import com.blazemeter.taurus.junit.CustomRunnerTest;
import com.blazemeter.taurus.junit.generator.Counter;
import com.blazemeter.taurus.junit.reporting.TaurusReporter;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.io.File;

import static org.junit.Assert.*;

public class JUnit4ListenerTest {
    @Test
    public void test() throws Exception {
        File tmp = File.createTempFile("tmp", ".ldjson");
        tmp.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(tmp.getAbsolutePath());

        JUnit4Listener listener = new JUnit4Listener(reporter, new Counter());
        Description description = Description.createSuiteDescription(JUnit4ListenerTest.class);

        listener.testStarted(description);
        listener.testAssumptionFailure(new Failure(description, new RuntimeException("failed")));
        listener.testFinished(description);
        reporter.close();

        String s = CustomRunnerTest.readFileToString(tmp);
        assertTrue(s, s.contains("RuntimeException: failed"));
    }
}