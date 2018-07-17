package com.blazemeter.taurus.junit;

import com.blazemeter.taurus.junit.reporting.Sample;

public interface Reporter {
    void writeSample(Sample sample);

    void close() throws Exception;

    boolean isVerbose();

    boolean isStopped();
}
