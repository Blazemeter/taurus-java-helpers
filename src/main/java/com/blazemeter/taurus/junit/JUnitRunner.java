package com.blazemeter.taurus.junit;

import com.blazemeter.taurus.junit.reporting.TaurusReporter;

import java.util.List;
import java.util.Properties;

public interface JUnitRunner {

    Object createRequest(List<Class> classes, Properties props);

    void executeRequest(Object requestItem, TaurusReporter reporter);
}
