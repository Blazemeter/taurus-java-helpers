package com.blazemeter.taurus.junit;


import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Properties;

public class CustomRunnerTest extends TestCase {

    public static int getLinesCount(File log) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(log));
        reader.skip(Long.MAX_VALUE);
        reader.close();
        return reader.getLineNumber();
    }

    private static String readFileToString(File log) throws IOException {
        return FileUtils.readFileToString(log);
    }

    public void testMain() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty("myProperty", "myValue");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        System.clearProperty("myProperty");
        CustomRunner.main(args);

        assertEquals(1, getLinesCount(report));
        assertNull(System.getProperty(CustomRunner.REPORT_FILE));
        assertEquals("myValue", System.getProperty("myProperty"));
    }

    public void testIterations() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(3));

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertEquals(3, getLinesCount(report));
    }

    public void testHold() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertTrue(2 < getLinesCount(report));
    }

    public void testHoldIterations() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertEquals(1, getLinesCount(report));
    }

    public void testRunIncludeCategories() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryA");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunExcludeCategories() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "categories.CategoryB");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
    }

    public void testRunIncludeAndExcludeCategories() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryA");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "categories.CategoryB");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 1, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
    }

    public void testRunIncludeAllCategories() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryA,categories.CategoryB");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 5, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunExcludeAllCategories() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "categories.CategoryA,categories.CategoryB");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 3, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
    }

    public void testRunItems() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1#flow1,testcases.subpackage.TestClass2#test2,testcases.TestClass4");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunAll() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 8, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testMethodNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1#flow3,testcases.TestClass2#test2");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be NoSuchMethodException");
        } catch (Exception e) {
            assertEquals("Method not found: testcases.TestClass1#flow3", e.getMessage());
        }
    }

    public void testClassNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass77,testcases.TestClass2#test2");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class not found: testcases.TestClass77", e.getMessage());
        }
    }
}