package com.blazemeter.taurus.junit;

import java.util.List;
import java.util.Properties;

public interface JUnitRunner {

    Object createRequest(List<Class> classes, Properties props);

    void executeRequest(Object requestItem, Reporter reporter, ThreadCounter counter);
}
