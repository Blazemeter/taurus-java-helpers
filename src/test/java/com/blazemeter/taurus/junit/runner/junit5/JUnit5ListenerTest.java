package com.blazemeter.taurus.junit.runner.junit5;

import categories.TestCategory;
import com.blazemeter.taurus.junit.CustomRunnerTest;
import com.blazemeter.taurus.junit.generator.Counter;
import com.blazemeter.taurus.reporting.Sample;
import com.blazemeter.taurus.reporting.TaurusReporter;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestIdentifier;
import testcases.subpackage.TestCase5;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.platform.engine.TestExecutionResult.*;

@Category(TestCategory.class)
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

    public void testFlow() throws Exception {
        File tmp = File.createTempFile("tmp", ".ldjson");
        tmp.deleteOnExit();

        TaurusReporter reporter = new TaurusReporter(tmp.getAbsolutePath());

        JUnit5Listener listener = new JUnit5Listener(reporter, new Counter());
        Method method = TestCase5.class.getDeclaredMethod("testJUnit5Method");
        TestIdentifier identifier = TestIdentifier.from(new TestMethodTestDescriptor(UniqueId.forEngine("123"), TestCase5.class, method));
        listener.startSample(TestIdentifier.from(new ClassTestDescriptor(UniqueId.forEngine("123"), TestCase5.class)));
        listener.executionStarted(identifier);
        listener.executionFinished(identifier, TestExecutionResult.failed(new RuntimeException("failed")));

        reporter.close();

        String s = CustomRunnerTest.readFileToString(tmp);
        assertTrue(s, s.contains("RuntimeException: failed"));
    }

    public void testTestStatus() throws IOException {
        Throwable e = null;
        try {
            org.junit.jupiter.api.Assertions.assertEquals("1", "");
        } catch (Throwable ex) {
            e = ex;
        }
        assertNotNull(e);
        File tmp = File.createTempFile("tmp", ".ldjson");
        tmp.deleteOnExit();
        TaurusReporter reporter = new TaurusReporter(tmp.getAbsolutePath());
        JUnit5Listener listener = new JUnit5Listener(reporter, new Counter());
        String status = listener.getStatusFromThrowableType(e);
        assertEquals(Sample.STATUS_FAILED, status);

        status = listener.getStatusFromThrowableType(new RuntimeException(""));
        assertEquals(Sample.STATUS_BROKEN, status);
    }
}