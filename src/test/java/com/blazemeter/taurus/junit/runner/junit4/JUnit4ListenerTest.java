package com.blazemeter.taurus.junit.runner.junit4;

import categories.TestCategory;
import com.blazemeter.taurus.junit.CustomRunnerTest;
import com.blazemeter.taurus.junit.generator.Counter;
import com.blazemeter.taurus.reporting.Sample;
import com.blazemeter.taurus.reporting.TaurusReporter;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;

@Category(TestCategory.class)
public class JUnit4ListenerTest extends TestCase {
    public void testAssumptionFailure() throws Exception {
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

    public void testFailure() throws Exception {
        File tmp = File.createTempFile("tmp", ".ldjson");
        tmp.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(tmp.getAbsolutePath());

        JUnit4Listener listener = new JUnit4Listener(reporter, new Counter());
        Description description = Description.createSuiteDescription(JUnit4ListenerTest.class);

        listener.testStarted(description);
        listener.testFailure(new Failure(description, new RuntimeException("failed")));
        listener.testFinished(description);
        reporter.close();

        String s = CustomRunnerTest.readFileToString(tmp);
        assertTrue(s, s.contains("RuntimeException: failed"));
    }

    public void testTestStatus() throws IOException {
        File tmp = File.createTempFile("tmp", ".ldjson");
        tmp.deleteOnExit();
        TaurusReporter reporter = new TaurusReporter(tmp.getAbsolutePath());
        JUnit4Listener listener = new JUnit4Listener(reporter, new Counter());

        Throwable e = null;
        try {
            org.junit.Assert.assertEquals("1", "");
        } catch (Throwable ex) {
            e = ex;
        }
        assertNotNull(e);

        String status = listener.getStatusFromThrowableType(e);
        assertEquals(Sample.STATUS_FAILED, status);

        e = null;
        try {
            org.junit.Assert.assertThat("1", is(""));
        } catch (Throwable ex) {
            e = ex;
        }
        assertNotNull(e);
        status = listener.getStatusFromThrowableType(e);
        assertEquals(Sample.STATUS_FAILED, status);

        status = listener.getStatusFromThrowableType(new RuntimeException(""));
        assertEquals(Sample.STATUS_BROKEN, status);
    }
}