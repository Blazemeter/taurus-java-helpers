package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.TaurusReporter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.EXCLUDE_CATEGORY;
import static com.blazemeter.taurus.junit.CustomRunner.HOLD;
import static com.blazemeter.taurus.junit.CustomRunner.INCLUDE_CATEGORY;
import static com.blazemeter.taurus.junit.CustomRunner.ITERATIONS;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;

public class JUnit5Runner {
    private static final Logger log = Logger.getLogger(JUnit5Runner.class.getName());

    public static void run(ArrayList<Class> classes, Properties props, TaurusReporter reporter) throws Exception {

        List<DiscoverySelector> selectors = new ArrayList<>();
        for (Class cls : classes) {
            selectors.add(selectClass(cls));
        }

        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request().selectors(selectors);

        if (null != props.getProperty(INCLUDE_CATEGORY)) {
            builder.filters(includeTags(props.getProperty(INCLUDE_CATEGORY).split(",")));
        }

        if (null != props.getProperty(EXCLUDE_CATEGORY)) {
            builder.filters(excludeTags(props.getProperty(EXCLUDE_CATEGORY).split(",")));
        }

        LauncherDiscoveryRequest request = builder.build();
        Launcher launcher = LauncherFactory.create();
        TestExecutionListener jUnit5Listener = new JUnit5Listener(reporter);
        launcher.registerTestExecutionListeners(jUnit5Listener);

        long iterations = Long.valueOf(props.getProperty(ITERATIONS, "0"));
        float hold = Float.valueOf(props.getProperty(HOLD, "0"));
        if (iterations == 0) {
            if (hold > 0) {
                iterations = Long.MAX_VALUE;
            } else {
                iterations = 1;
            }
        }

        long startTime = System.currentTimeMillis();
        for (int iteration = 0; iteration < iterations; iteration++) {
            launcher.execute(request);
            log.info("Elapsed: " + (System.currentTimeMillis() - startTime) + ", limit: " + (hold * 1000));
            if (hold > 0 && System.currentTimeMillis() - startTime > hold * 1000) {
                log.info("Duration limit reached, stopping");
                break;
            }
        }


        reporter.close();
    }
}
