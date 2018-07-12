package com.blazemeter.taurus.junit;

import com.blazemeter.taurus.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit5.JUnit5Runner;
import junit.framework.TestCase;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
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
    public static final String RUN_ITEMS = "run_items";
    public static final String JUNIT_5 = "junit5";

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
        log.info("Running with classes: " + classes.toString());

        passToSystemProperties(props);

        JUnitRunner runner = getJUnitRunner(null != props.getProperty(JUNIT_5));

        Object request = runner.createRequest(classes, props);
        TaurusReporter reporter = new TaurusReporter(props.getProperty(REPORT_FILE));

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
            runner.executeRequest(request, reporter);
            log.info("Elapsed: " + (System.currentTimeMillis() - startTime) + ", limit: " + (hold * 1000));
            if (hold > 0 && System.currentTimeMillis() - startTime > hold * 1000) {
                log.info("Duration limit reached, stopping");
                break;
            }
        }

        reporter.close();
    }

    private static JUnitRunner getJUnitRunner(boolean isJUnit5) {
        return isJUnit5 ?
                new JUnit5Runner() :
                new JUnit4Runner();
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
                || INCLUDE_CATEGORY.equals(propName)
                || EXCLUDE_CATEGORY.equals(propName)
                || RUN_ITEMS.equals(propName)
                || JUNIT_5.equals(propName)
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

        URL url = new URL("file:" + jar_path);
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
