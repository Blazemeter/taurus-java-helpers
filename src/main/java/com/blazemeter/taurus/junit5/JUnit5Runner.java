package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.TaurusReporter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class JUnit5Runner {

    public static void run(ArrayList<Class> classes, Properties props, TaurusReporter reporter) throws Exception {

        List<DiscoverySelector> selectors = new ArrayList<>();
        for (Class cls : classes) {
            selectors.add(selectClass(cls));
        }

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                    selectors
                )
//                .filters(
//                        includeClassNamePatterns(".*Tests")
//                )
                .build();

        Launcher launcher = LauncherFactory.create();

// Register a listener of your choice
        TestExecutionListener summaryGeneratingListener = new SummaryGeneratingListener();
        TestExecutionListener jUnit5Listener = new JUnit5Listener(reporter);

        launcher.registerTestExecutionListeners(summaryGeneratingListener);
        launcher.registerTestExecutionListeners(jUnit5Listener);

        launcher.execute(request);

        reporter.close();
    }
}
