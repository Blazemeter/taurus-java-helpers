package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.CustomListener;
import com.blazemeter.taurus.junit.Sample;
import com.blazemeter.taurus.junit.TaurusReporter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.logging.Logger;

public class JUnit5Listener implements TestExecutionListener {
    private static final Logger log = Logger.getLogger(CustomListener.class.getName());
    private Sample pendingSample;
    private TaurusReporter reporter;
    private long started = 0;

    private long testCount = 0;
    private long failedCount = 0;
    private final static String report_tmpl = "%s.%s,Total:%d Passed:%d Failed:%d\n";

    public JUnit5Listener(TaurusReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        log.info("Test Plan Started");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("Test Plan Finished");
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        log.info(String.format("started %s", testIdentifier.getDisplayName()));
        started = System.currentTimeMillis();
        pendingSample = new Sample();
//        pendingSample.setLabel(testIdentifier.getMethodName());
//        pendingSample.setSuite(description.getClassName());
//        pendingSample.setFullName(description.getClassName() + "." + description.getMethodName());
        testCount += 1;
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        log.info(String.format("finished %s", testIdentifier.getDisplayName()));
        double duration = (System.currentTimeMillis() - started) / 1000.0;
        pendingSample.setDuration(duration);
        reporter.writeSample(pendingSample);

        if (!pendingSample.isSuccessful()) {
            failedCount += 1;
        }
        pendingSample = null;
//        System.out.printf(report_tmpl,
//                description.getClassName(),
//                description.getMethodName(),
//                testCount,
//                testCount - failedCount,
//                failedCount);
    }
}
