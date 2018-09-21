package com.blazemeter.taurus.junit.runner.junit5;

import com.blazemeter.taurus.junit.CustomListener;
import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.reporting.Sample;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;
import java.util.logging.Logger;

public class JUnit5Listener extends CustomListener implements TestExecutionListener {
    private static final Logger log = Logger.getLogger(JUnit5Listener.class.getName());

    public JUnit5Listener(Reporter reporter, ThreadCounter counter) {
        super(reporter, counter);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if (isVerbose()) {
            log.info("Test Plan Started");
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (isVerbose()) {
            log.info("Test Plan Finished, successful=" + (getFailedCount() == 0) + ", run count=" + (getTestCount() - getSkippedCount()));
        }
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
                finishSample(getStatusFromThrowableType(throwable), exceptionName + ": " + throwable.getMessage(), throwable);
            } else {
                finishSample(status, null, null);
            }
        }
    }

    protected String getStatusFromThrowableType(Throwable exception) {
        if (exception instanceof AssertionFailedError) {
            return Sample.STATUS_FAILED;
        }
        return Sample.STATUS_BROKEN;
    }

    protected String getStatus(TestExecutionResult.Status status) {
        switch (status) {
            case SUCCESSFUL:
                return Sample.STATUS_PASSED;
            case FAILED:
                if (isVerbose()) {
                    log.severe(String.format("failed %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                }
                return Sample.STATUS_FAILED;
            case ABORTED:
                if (isVerbose()) {
                    log.severe(String.format("aborted %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                }
                return Sample.STATUS_BROKEN;
                default:
                    if (isVerbose()) {
                        log.severe(String.format("failed %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                    }
                    return Sample.STATUS_FAILED;
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        startSample(testIdentifier);
        if (isVerbose()) {
            log.warning(String.format("ignored %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
        }
        finishSample(Sample.STATUS_SKIPPED, reason, null);
    }

}
