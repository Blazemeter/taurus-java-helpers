package com.blazemeter.taurus.junit;

import java.util.logging.Logger;

public class CustomListener {
    private static final Logger log = Logger.getLogger(CustomListener.class.getName());

    protected Sample pendingSample;
    private TaurusReporter reporter;
    private long started = 0;

    private long testCount = 0;
    private long failedCount = 0;
    private long skippedCount = 0;
    private final static String report_tmpl = "%s.%s, Total:%d Passed:%d Failed:%d Skipped:%d\n";

    public CustomListener(TaurusReporter reporter) {
        this.reporter = reporter;
    }

    public void startSample(String methodName, String className) {
        testCount++;
        log.info(String.format("started %s(%s)", methodName, className));
        started = System.currentTimeMillis();
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
        log.info(String.format("finished %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
        double duration = (finishTime - started) / 1000.0;
        pendingSample.setDuration(duration);

        reporter.writeSample(pendingSample);
        if (pendingSample.isSkipped()) {
            skippedCount++;
        } else if (!pendingSample.isSuccessful()) {
            failedCount += 1;
        }

        System.out.printf(report_tmpl,
                pendingSample.getSuite(),
                pendingSample.getLabel(),
                testCount,
                getPassedCount(),
                failedCount,
                skippedCount);
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
}
