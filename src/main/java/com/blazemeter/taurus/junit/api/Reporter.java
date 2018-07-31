package com.blazemeter.taurus.junit.api;

import com.blazemeter.taurus.reporting.Sample;

public interface Reporter {
    void writeSample(Sample sample);

    void close() throws Exception;

    boolean isVerbose();

    boolean isStopped();
}
