package com.blazemeter.taurus.junit.runner.junit4;

import com.blazemeter.taurus.junit.CustomListener;
import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.reporting.Sample;
import com.blazemeter.taurus.utils.Utils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.logging.Logger;

public class JUnit4Listener extends RunListener {

    private static final Logger log = Logger.getLogger(JUnit4Listener.class.getName());
    private final CustomListener listener;
    private final boolean isVerbose;

    public JUnit4Listener(Reporter reporter, ThreadCounter counter) {
        super();
        listener = new CustomListener(reporter, counter);
        isVerbose = reporter.isVerbose();
    }

    public void testRunStarted(Description description) {
        if (isVerbose) {
            log.info("Run Started: " + description.getDisplayName());
        }
    }

    public void testRunFinished(Result result) throws Exception {
        if (isVerbose) {
            log.info("Run Finished, successful=" + result.wasSuccessful() + ", run count=" + result.getRunCount());
        }
    }

    public void testStarted(Description description) throws Exception {
        listener.startSample(description.getMethodName(), description.getClassName());
    }

    public void testFinished(Description description) throws Exception {
        listener.finishSample();
    }

    public void testFailure(Failure failure) throws Exception {
        if (isVerbose) {
            log.severe(String.format("failed %s", failure.toString()));
        }
        Throwable exception = failure.getException();
        listener.getPendingSample().setStatus(getStatusFromThrowableType(exception));
        String exceptionName = exception.getClass().getName();
        listener.getPendingSample().setErrorMessage(exceptionName + ": " + failure.getMessage());
        listener.getPendingSample().setErrorTrace(Utils.getStackTrace(failure.getException()));
    }

    protected String getStatusFromThrowableType(Throwable exception) {
        if (exception instanceof AssertionError) {
            return Sample.STATUS_FAILED;
        }
        return Sample.STATUS_BROKEN;
    }

    public void testAssumptionFailure(Failure failure) {
        if (isVerbose) {
            log.severe(String.format("assert failed %s", failure.toString()));
        }
        listener.getPendingSample().setStatus(Sample.STATUS_FAILED);
        String exceptionName = failure.getException().getClass().getName();
        listener.getPendingSample().setErrorMessage(exceptionName + ": " + failure.getMessage());
        listener.getPendingSample().setErrorTrace(Utils.getStackTrace(failure.getException()));
    }

    public void testIgnored(Description description) throws Exception {
        if (isVerbose) {
            log.warning(String.format("ignored %s", description.getDisplayName()));
        }
        testStarted(description);
        listener.finishSample(Sample.STATUS_SKIPPED, description.getDisplayName(), null);
    }

}
