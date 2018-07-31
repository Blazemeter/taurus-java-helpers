package com.blazemeter.taurus.junit.runner.junit4;

import com.blazemeter.taurus.classpath.ClasspathScanner;
import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.JUnitRunner;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
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

    private final ClasspathScanner classpathScanner;

    public JUnit4Runner() {
        classpathScanner = createClasspathScanner();
    }


    @Override
    public Request createRequest(Properties props) {
        String runItems = props.getProperty(RUN_ITEMS);
        if (runItems != null) {
            log.info("Create JUnit 4 request with following items: " + runItems);
            return JUnitRequest.createItemsRequest(runItems, classpathScanner);
        } else {
            String[] junitArguments = generateArgs(props);
            log.info("Create JUnit 4 request with following arguments: " + Arrays.toString(junitArguments));
            return JUnitRequest.createCategoryRequest(junitArguments);
        }
    }

    @Override
    public void executeRequest(Object requestItem, Reporter reporter, ThreadCounter counter) {
        JUnit4Listener listener = new JUnit4Listener(reporter, counter);
        JUnitCore runner = new JUnitCore();
        runner.addListener(listener);
        runner.run((Request) requestItem);
    }

    private String[] generateArgs(Properties props) {
        List<String> args = new ArrayList<>();

        // Category options should be first!!!
        if (null != props.getProperty(INCLUDE_CATEGORY)) {
            addFilter(args, IncludeCategories.class.getName(), props.getProperty(INCLUDE_CATEGORY));
        }

        if (null != props.getProperty(EXCLUDE_CATEGORY)) {
            addFilter(args, ExcludeCategories.class.getName(), props.getProperty(EXCLUDE_CATEGORY));
        }

        List<Class> classes = classpathScanner.getAllTestClasses(getClassLoader());
        if (classes.isEmpty()) {
            throw new CustomRunnerException("Nothing to test");
        }
        for (Class c : classes) {
            args.add(c.getName());
        }

        return args.toArray(new String[0]);
    }

    protected ClasspathScanner createClasspathScanner() {
        return new ClasspathScanner(new JUnit4ClassFilter());
    }

    protected ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    private static void addFilter(List<String> args, String name, String property) {
        args.add("--filter=" + name + '=' + property);
    }

}
