package com.blazemeter.taurus.junit;

import categories.TestCategory;
import com.blazemeter.taurus.junit.api.JUnitRunner;
import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.reporting.Sample;
import org.junit.experimental.categories.Category;

import java.util.Properties;

@Category(TestCategory.class)
public class TestJUnitRunner implements JUnitRunner {
    @Override
    public Object createRequest(Properties props) {
        return null;
    }

    @Override
    public void executeRequest(Object requestItem, Reporter reporter, ThreadCounter counter) {
        Sample sample = new Sample();
        sample.setLabel("aaaa");
        sample.setActiveThreads(counter.getActiveThreads());
        reporter.writeSample(sample);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
