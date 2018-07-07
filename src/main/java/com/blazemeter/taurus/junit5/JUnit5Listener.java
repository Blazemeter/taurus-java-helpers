package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.CustomListener;
import com.blazemeter.taurus.junit.Sample;
import com.blazemeter.taurus.junit.TaurusReporter;
import com.blazemeter.taurus.junit.Utils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Optional;
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
        log.info("Run Finished, successful=" + (failedCount == 0) +", run count=" + testCount);

    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            startSample(testIdentifier);
            testCount += 1;
        }
    }

    protected void startSample(TestIdentifier testIdentifier) {
        Optional<TestSource> source = testIdentifier.getSource();
        if (source.isPresent()) {
            TestSource testSource = testIdentifier.getSource().get();
            if (testSource instanceof MethodSource) {
                startSample((MethodSource) testSource);
            } else {
                //TODO: other test source..
            }
        }
    }

    protected void startSample(MethodSource source) {
        log.info(String.format("started %s(%s)", source.getMethodName(), source.getClassName()));
        started = System.currentTimeMillis();
        pendingSample = new Sample();
        pendingSample.setLabel(source.getMethodName());
        pendingSample.setSuite(source.getClassName());
        pendingSample.setFullName(source.getClassName() + "." + source.getMethodName());
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String status = getStatus(testExecutionResult.getStatus());
            Optional<Throwable> optional = testExecutionResult.getThrowable();
            if (optional.isPresent()) {
                Throwable throwable = optional.get();
                finishSample(status, throwable.getMessage(), throwable);
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
                return Sample.STATUS_FAILED;
            case ABORTED:
                return Sample.STATUS_BROKEN;
                default:
                    return Sample.STATUS_FAILED;
        }
    }

    //todo: print log about fails/abort etc..
    private void finishSample(String status, String msg, Throwable ex) {
        log.info(String.format("finished %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
        double duration = (System.currentTimeMillis() - started) / 1000.0;
        pendingSample.setDuration(duration);
        pendingSample.setStatus(status);
        pendingSample.setErrorMessage(msg);
        if (ex != null) {
            pendingSample.setErrorTrace(Utils.getStackTrace(ex));
        }

        reporter.writeSample(pendingSample);

        if (!pendingSample.isSuccessful()) {
            failedCount += 1;
        }
        System.out.printf(report_tmpl,
                pendingSample.getSuite(),
                pendingSample.getLabel(),
                testCount,
                testCount - failedCount,
                failedCount);
        pendingSample = null;
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        startSample(testIdentifier);
        finishSample(Sample.STATUS_SKIPPED, reason, null);
    }


}
