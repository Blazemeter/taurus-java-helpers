package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.TaurusReporter;
import org.junit.internal.Classes;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.blazemeter.taurus.junit.CustomRunner.EXCLUDE_CATEGORY;
import static com.blazemeter.taurus.junit.CustomRunner.HOLD;
import static com.blazemeter.taurus.junit.CustomRunner.INCLUDE_CATEGORY;
import static com.blazemeter.taurus.junit.CustomRunner.ITERATIONS;
import static com.blazemeter.taurus.junit.CustomRunner.RUN_ITEMS;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.runner.JUnitRequest.checkMethod;

public class JUnit5Runner {
    private static final Logger log = Logger.getLogger(JUnit5Runner.class.getName());

    public static void run(ArrayList<Class> classes, Properties props, TaurusReporter reporter) throws Exception {

        List<DiscoverySelector> selectors = getSelectors(classes, props);

        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request().selectors(selectors);


        LauncherDiscoveryRequest request = addFilters(builder, props).build();
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

    private static LauncherDiscoveryRequestBuilder addFilters(LauncherDiscoveryRequestBuilder builder, Properties props) {
        Map<FiltersType, List<String>> filtersMap = new HashMap<>();
        String includeFilters = props.getProperty(INCLUDE_CATEGORY);
        if (null != includeFilters) {
            for (String filter : includeFilters.split(",")) {
                detectFilter(filter, true, filtersMap);
            }
        }

        String excludeFilters = props.getProperty(EXCLUDE_CATEGORY);
        if (null != excludeFilters) {
            for (String filter : excludeFilters.split(",")) {
                detectFilter(filter, false, filtersMap);
            }
        }

        builder.filters(convertFilters(filtersMap));

//         todo: do we need exclude old engine here?
//        builder.filters(EngineFilter.excludeEngines("junit-vintage"));
        return builder;
    }

    private static Filter[] convertFilters(Map<FiltersType, List<String>> filtersMap) {
        List<Filter> res = new ArrayList<>();
        if (filtersMap.containsKey(FiltersType.INCLUDE_TAGS)) {
            res.add(includeTags(filtersMap.get(FiltersType.INCLUDE_TAGS)));
        }

        if (filtersMap.containsKey(FiltersType.EXCLUDE_TAGS)) {
            res.add(excludeTags(filtersMap.get(FiltersType.EXCLUDE_TAGS)));
        }

        if (filtersMap.containsKey(FiltersType.INCLUDE_PACKAGES)) {
            res.add(includePackageNames(filtersMap.get(FiltersType.INCLUDE_PACKAGES)));
        }

        if (filtersMap.containsKey(FiltersType.EXCLUDE_PACKAGES)) {
            res.add(excludePackageNames(filtersMap.get(FiltersType.EXCLUDE_PACKAGES)));
        }
        return res.toArray(new Filter[0]);
    }

    private enum FiltersType {
        INCLUDE_TAGS,
        EXCLUDE_TAGS,
        INCLUDE_PACKAGES,
        EXCLUDE_PACKAGES
    }

    private static void addFilter(String newFilter, FiltersType filtersType, Map<FiltersType, List<String>> filtersMap) {
        List<String> filters = filtersMap.computeIfAbsent(filtersType, k -> new ArrayList<>());
        filters.add(newFilter);
    }

    private static void detectFilter(String filter, boolean isInclude, Map<FiltersType, List<String>> filtersMap) {
        try {
            Classes.getClass(filter);
            if (isInclude) {
                addFilter(filter, FiltersType.INCLUDE_TAGS, filtersMap);
            } else {
                addFilter(filter, FiltersType.EXCLUDE_TAGS, filtersMap);
            }
            return;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.log(Level.FINER, "Filter class not found: " + filter, e);
        }

        Package pack = Package.getPackage(filter);
        if (pack == null) {
            log.log(Level.SEVERE, "Filter Class or Package not found: " + filter);
            throw new RuntimeException("Filter Class or Package not found: " + filter);
        }
        if (isInclude) {
            addFilter(filter, FiltersType.INCLUDE_PACKAGES, filtersMap);
        } else {
            addFilter(filter, FiltersType.EXCLUDE_PACKAGES, filtersMap);
        }
    }

    private static List<DiscoverySelector> getSelectors(ArrayList<Class> classes, Properties props) {
        final List<DiscoverySelector> selectors = new ArrayList<>();
        String runItems = props.getProperty(RUN_ITEMS);
        if (runItems != null) {
            log.info("Create JUnit 5 request with following items: " + runItems);
            String[] items = runItems.split(",");
            for (String item : items) {
                selectors.add(getSelector(item));
            }
        } else {
            for (Class cls : classes) {
                selectors.add(selectClass(cls));
            }
        }

        return selectors;
    }

    private static DiscoverySelector getSelector(String item) {
        try {
            if (item.contains("#")) {
                String[] classAndMethod = item.split("#");
                Class<?> cls = Classes.getClass(classAndMethod[0]);

                checkMethod(cls, classAndMethod[1]);
                return selectMethod(cls, classAndMethod[1]);
            } else {
                return getClassOrPackageSelector(item);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.log(Level.SEVERE, "Class not found: " + item, e);
            throw new RuntimeException("Class not found: " + item, e);
        } catch (NoSuchMethodException e) {
            log.log(Level.SEVERE, "Method not found: " + item, e);
            throw new RuntimeException("Method not found: " + item, e);
        }
    }

    private static DiscoverySelector getClassOrPackageSelector(String item) {
        try {
            Class<?> cls = Classes.getClass(item);
            return selectClass(cls);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.log(Level.FINE, "Class not found: " + item, e);
        }

        // does not work
        Package pack = Package.getPackage(item);
        if (pack == null) {
            log.log(Level.SEVERE, "Class or Package not found: " + item);
            throw new RuntimeException("Class or Package not found: " + item);
        }

        return selectPackage(item);
    }


}
