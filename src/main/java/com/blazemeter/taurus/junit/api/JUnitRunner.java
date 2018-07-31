package com.blazemeter.taurus.junit.api;

import java.util.Properties;

public interface JUnitRunner {

    Object createRequest(Properties props);

    void executeRequest(Object requestItem, Reporter reporter, ThreadCounter counter);
}
