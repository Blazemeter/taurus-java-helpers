package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.CustomListener;
import com.blazemeter.taurus.junit.Sample;
import com.blazemeter.taurus.junit.TaurusReporter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Optional;
import java.util.logging.Logger;

public class JUnit5Listener extends CustomListener implements TestExecutionListener {
    private static final Logger log = Logger.getLogger(JUnit5Listener.class.getName());

    public JUnit5Listener(TaurusReporter reporter) {
        super(reporter);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        log.info("Test Plan Started");
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("Test Plan Finished, successful=" + (getFailedCount() == 0) +", run count=" + (getTestCount() - getSkippedCount()));
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            startSample(testIdentifier);
        }
    }

    protected void startSample(TestIdentifier testIdentifier) {
        Optional<TestSource> source = testIdentifier.getSource();
        if (source.isPresent()) {
            TestSource testSource = testIdentifier.getSource().get();
            if (testSource instanceof MethodSource) {
                MethodSource src = (MethodSource) testSource;
                startSample(src.getMethodName(), src.getClassName());
            } else {
                log.severe("Unsupported test source: " + testSource.getClass().getName());
                //TODO: other test source..
            }
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String status = getStatus(testExecutionResult.getStatus());
            Optional<Throwable> optional = testExecutionResult.getThrowable();
            if (optional.isPresent()) {
                Throwable throwable = optional.get();
                String exceptionName = throwable.getClass().getName();
                finishSample(status, exceptionName + ": " + throwable.getMessage(), throwable);
            } else {
                finishSample(status, null, null);
            }
        }
    }

    private String getStatus(TestExecutionResult.Status status) {
        switch (status) {
            case SUCCESSFUL:
                return Sample.STATUS_PASSED;
            case FAILED:
                log.severe(String.format("failed %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                return Sample.STATUS_FAILED;
            case ABORTED:
                log.severe(String.format("aborted %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                return Sample.STATUS_BROKEN;
                default:
                    log.severe(String.format("failed %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                    return Sample.STATUS_FAILED;
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        startSample(testIdentifier);
        log.warning(String.format("ignored %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
        finishSample(Sample.STATUS_SKIPPED, reason, null);
    }

}
