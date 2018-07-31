package com.blazemeter.taurus.junit;

import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.reporting.Sample;
import com.blazemeter.taurus.utils.Utils;

import java.util.logging.Logger;

public class CustomListener {
    private static final Logger log = Logger.getLogger(CustomListener.class.getName());

    protected Sample pendingSample;
    private Reporter reporter;
    private ThreadCounter counter;

    private long testCount = 0;
    private long failedCount = 0;
    private long skippedCount = 0;

    private final boolean isVerbose;

    private final static String report_tmpl = "%s.%s, Total:%d Passed:%d Failed:%d Skipped:%d\n";

    public CustomListener(Reporter reporter, ThreadCounter counter) {
        this.reporter = reporter;
        this.isVerbose = reporter.isVerbose();
        this.counter = counter;
    }

    public void startSample(String methodName, String className) {
        if (isVerbose) {
            log.info(String.format("started %s(%s)", methodName, className));
        }
        testCount++;
        pendingSample = new Sample();
        pendingSample.setLabel(methodName);
        pendingSample.setSuite(className);
        pendingSample.setFullName(className + "." + methodName);
    }

    public void finishSample(String status, String msg, Throwable ex) {
        long finishTime = System.currentTimeMillis();
        pendingSample.setStatus(status);
        pendingSample.setErrorMessage(msg);
        if (ex != null) {
            pendingSample.setErrorTrace(Utils.getStackTrace(ex));
        }
        finishSample(finishTime);
    }

    public void finishSample() {
        finishSample(System.currentTimeMillis());
    }

    private void finishSample(long finishTime) {
        if (isVerbose) {
            log.info(String.format("finished %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
        }
        long duration = finishTime - pendingSample.getStartTime();
        pendingSample.setDuration(duration);
        pendingSample.setActiveThreads(counter.getActiveThreads());

        reporter.writeSample(pendingSample);
        if (pendingSample.isSkipped()) {
            skippedCount++;
        } else if (!pendingSample.isSuccessful()) {
            failedCount += 1;
        }

        if (isVerbose) {
            System.out.printf(report_tmpl,
                    pendingSample.getSuite(),
                    pendingSample.getLabel(),
                    testCount,
                    getPassedCount(),
                    failedCount,
                    skippedCount);
        }
        pendingSample = null;
    }

    public Sample getPendingSample() {
        return pendingSample;
    }

    public long getTestCount() {
        return testCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public long getSkippedCount() {
        return skippedCount;
    }

    public long getPassedCount() {
        return testCount - failedCount - skippedCount;
    }

    public boolean isVerbose() {
        return isVerbose;
    }
}
