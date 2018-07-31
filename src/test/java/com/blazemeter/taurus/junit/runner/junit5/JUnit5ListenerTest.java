package com.blazemeter.taurus.junit.runner.junit5;

import com.blazemeter.taurus.junit.generator.Counter;
import com.blazemeter.taurus.reporting.Sample;
import com.blazemeter.taurus.reporting.TaurusReporter;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static org.junit.platform.engine.TestExecutionResult.*;

public class JUnit5ListenerTest extends TestCase {
    public void test() throws IOException {
        File tmp = File.createTempFile("tmp", ".ldjson");
        tmp.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(tmp.getAbsolutePath());

        JUnit5Listener listener = new JUnit5Listener(reporter, new Counter());
        listener.startSample("methodName", "className");
        assertEquals(Sample.STATUS_PASSED, listener.getStatus(Status.SUCCESSFUL));
        assertEquals(Sample.STATUS_FAILED, listener.getStatus(Status.FAILED));
        assertEquals(Sample.STATUS_BROKEN, listener.getStatus(Status.ABORTED));
    }
}