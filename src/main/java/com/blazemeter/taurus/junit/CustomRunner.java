package com.blazemeter.taurus.junit;

import com.blazemeter.taurus.junit.generator.Supervisor;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CustomRunner {
    private static final Logger log = Logger.getLogger(CustomRunner.class.getName());
    public static final String REPORT_FILE = "report_file";
    public static final String ITERATIONS = "iterations";
    public static final String HOLD = "hold_for";
    public static final String CONCURRENCY = "concurrency";
    public static final String STEPS = "steps";
    public static final String RAMP_UP = "ramp_up";
    public static final String INCLUDE_CATEGORY = "include_category";
    public static final String EXCLUDE_CATEGORY = "exclude_category";
    public static final String RUN_ITEMS = "run_items";
    public static final String JUNIT_VERSION = "junit_version";

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

        passToSystemProperties(props);

        Supervisor supervisor = new Supervisor(props);
        supervisor.execute();
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
                || JUNIT_VERSION.equals(propName)
                || CONCURRENCY.equals(propName)
                || RAMP_UP.equals(propName)
                || STEPS.equals(propName);
    }

}
