package com.blazemeter.taurus.junit4;

import com.blazemeter.taurus.junit.TaurusReporter;
import com.blazemeter.taurus.junit.JUnitRunner;
import org.junit.experimental.categories.ExcludeCategories;
import org.junit.experimental.categories.IncludeCategories;
import org.junit.runner.JUnitCore;
import org.junit.runner.JUnitRequest;
import org.junit.runner.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.EXCLUDE_CATEGORY;
import static com.blazemeter.taurus.junit.CustomRunner.INCLUDE_CATEGORY;
import static com.blazemeter.taurus.junit.CustomRunner.RUN_ITEMS;

public class JUnit4Runner implements JUnitRunner {
    private static final Logger log = Logger.getLogger(JUnit4Runner.class.getName());

    @Override
    public Request createRequest(List<Class> classes, Properties props) {
        String runItems = props.getProperty(RUN_ITEMS);
        if (runItems != null) {
            log.info("Create JUnit request with following items: " + runItems);
            return JUnitRequest.createItemsRequest(runItems);
        } else {
            String[] junitArguments = generateArgs(classes, props);
            log.info("Create JUnit request with following arguments: " + Arrays.toString(junitArguments));
            return JUnitRequest.createCategoryRequest(junitArguments);
        }
    }

    @Override
    public void executeRequest(Object requestItem, TaurusReporter reporter) {
        JUnit4Listener listener = new JUnit4Listener(reporter);
        JUnitCore runner = new JUnitCore();
        runner.addListener(listener);
        runner.run((Request) requestItem);
    }

    private static String[] generateArgs(List<Class> classes, Properties props) {
        List<String> args = new ArrayList<>();

        // Category options should be first!!!
        if (null != props.getProperty(INCLUDE_CATEGORY)) {
            addFilter(args, IncludeCategories.class.getName(), props.getProperty(INCLUDE_CATEGORY));
        }

        if (null != props.getProperty(EXCLUDE_CATEGORY)) {
            addFilter(args, ExcludeCategories.class.getName(), props.getProperty(EXCLUDE_CATEGORY));
        }

        for (Class c : classes) {
            args.add(c.getName());
        }

        return args.toArray(new String[0]);
    }

    private static void addFilter(List<String> args, String name, String property) {
        args.add("--filter=" + name + '=' + property);
    }

}
