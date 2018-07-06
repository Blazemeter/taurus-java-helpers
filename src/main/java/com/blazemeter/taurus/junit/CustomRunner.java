package com.blazemeter.taurus.junit;

import junit.framework.TestCase;
import org.junit.experimental.categories.ExcludeCategories;
import org.junit.experimental.categories.IncludeCategories;
import org.junit.runner.JUnitRequest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CustomRunner {
    private static final Logger log = Logger.getLogger(CustomRunner.class.getName());
    public static final String REPORT_FILE = "report_file";
    public static final String TARGET_PREFIX = "target_";
    public static final String ITERATIONS = "iterations";
    public static final String HOLD = "hold_for";
    public static final String INCLUDE_CATEGORY = "include_category";
    public static final String EXCLUDE_CATEGORY = "exclude_category";
    public static final String TEST_SUITE = "test_suite";

    static {
        log.setLevel(Level.FINER);
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting: " + Arrays.toString(args));
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage requires 1 parameter, containing path to properties file");
        }

        Properties props = new Properties();
        props.load(new FileReader(args[0]));

        ArrayList<Class> classes = getClasses(props);

        if (classes.isEmpty()) {
            throw new RuntimeException("Nothing to test");
        }

        passToSystemProperties(props);

        log.info("Running with classes: " + classes.toString());
        TaurusReporter reporter = new TaurusReporter(props.getProperty(REPORT_FILE));
        CustomListener custom_listener = new CustomListener(reporter);

        JUnitCore runner = new JUnitCore();
        runner.addListener(custom_listener);

        Request request = createRequest(classes, props);

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
            runner.run(request);
            log.info("Elapsed: " + (System.currentTimeMillis() - startTime) + ", limit: " + (hold * 1000));
            if (hold > 0 && System.currentTimeMillis() - startTime > hold * 1000) {
                log.info("Duration limit reached, stopping");
                break;
            }
        }

        reporter.close();
    }

    private static Request createRequest(ArrayList<Class> classes, Properties props) {
        String testSuite = props.getProperty(TEST_SUITE);
        if (testSuite != null) {
            log.info("Create JUnit request for test suite: " + testSuite);
            return JUnitRequest.createSuiteRequest(testSuite);
        } else {
            String[] junitArguments = generateArgs(classes, props);
            log.info("Create JUnit request with following arguments: " + Arrays.toString(junitArguments));
            return JUnitRequest.createCategoryRequest(junitArguments);
        }
    }

    private static String[] generateArgs(ArrayList<Class> classes, Properties props) {
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

    protected static void passToSystemProperties(Properties props) {
        Enumeration<?> it = props.propertyNames();
        while (it.hasMoreElements()) {
            String propName = (String) it.nextElement();
            if (!isCustomRunnerProperty(propName)) {
                System.setProperty(propName, props.getProperty(propName));
            }
        }
    }

    protected static boolean isCustomRunnerProperty(String propName) {
        return REPORT_FILE.equals(propName)
                || ITERATIONS.equals(propName)
                || HOLD.equals(propName)
                || propName.startsWith(TARGET_PREFIX);
    }

    protected static ArrayList<Class> getClasses(Properties props) {
        ArrayList<Class> result = new ArrayList<>(0);

        Enumeration<?> it = props.propertyNames();
        while (it.hasMoreElements()) {
            String propName = (String) it.nextElement();
            if (propName.startsWith(TARGET_PREFIX)) {
                result.addAll(getClasses(props.getProperty(propName)));
            }
        }

        return result;
    }

    protected static List<Class<?>> getClasses(String jar_path) {
        List<Class<?>> test_classes = new ArrayList<>(); //List of loaded classes
        try {
            processJAR(test_classes, jar_path);
        } catch (IOException | ReflectiveOperationException e) {
            log.warning("Failed to add " + jar_path + "\n" + Utils.getStackTrace(e));
        }
        return test_classes;
    }

    protected static void processJAR(List<Class<?>> test_classes, String jar_path) throws IOException, ReflectiveOperationException {
        log.info("Processing JAR: " + jar_path);
        JarFile jarFile = new JarFile(jar_path);
        Enumeration<JarEntry> jar_entries_enum = jarFile.entries();

        URL url = new URL("jar:file:" + jar_path + "!/");
        addURL(url);
        URLClassLoader cl = URLClassLoader.newInstance(new URL[] {url});

        while (jar_entries_enum.hasMoreElements()) {
            JarEntry jar_entry = jar_entries_enum.nextElement();
            if (jar_entry.isDirectory() || !jar_entry.getName().endsWith(".class")) {
                continue;
            }

            String className = jar_entry.getName().substring(0, jar_entry.getName().length() - ".class".length());
            className = className.replace('/', '.');

            Class<?> c = cl.loadClass(className);
            log.info("TestCase.class.isAssignableFrom(" + c.getCanonicalName() + ") = " + TestCase.class.isAssignableFrom(c));
            log.info("has_annotations(" + c.getCanonicalName() + ") = " + has_annotations(c));

            if (Modifier.isAbstract(c.getModifiers())) {
                log.info("Skip because of abstract");
                continue;
            }

            if (TestCase.class.isAssignableFrom(c) || has_annotations(c)) {
                test_classes.add(c);
                log.info("class added to tests: " + c.getCanonicalName());
            }
        }
        jarFile.close();
    }

    private static void addURL(URL url) throws ReflectiveOperationException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(systemClassLoader, url);
    }

    protected static boolean has_annotations(Class<?> c) {
        for (Method method : c.getMethods()) {
            if (method.isAnnotationPresent(org.junit.Test.class)) {
                return true;
            }
        }

        return false;
    }

}
