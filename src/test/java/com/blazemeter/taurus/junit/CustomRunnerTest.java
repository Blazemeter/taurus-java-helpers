package com.blazemeter.taurus.junit;


import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertNull;

public class CustomRunnerTest {

    public static int getLinesCount(File log) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(log));
        reader.skip(Long.MAX_VALUE);
        reader.close();
        return reader.getLineNumber();
    }

    public static String readFileToString(File log) throws IOException {
        return FileUtils.readFileToString(log);
    }

    @Test
    public void testMainJUnit4() throws Exception {
        testMain("4");
    }

    @Test
    public void testMainJUnit5() throws Exception {
        testMain("5");
    }

    private void testMain(String junitVersion) throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty("myProperty", "myValue");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
    public void testIterationsJUnit4() throws Exception {
        testIterations("4");
    }

    @Test
    public void testIterationsJUnit5() throws Exception {
        testIterations("5");
    }

    private void testIterations(String junitVersion) throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(3));
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertEquals(3, getLinesCount(report));
    }

    @Test
    public void testHoldJUnit4() throws Exception {
        testHold("4");
    }

    @Test
    public void testHoldJUnit5() throws Exception {
        testHold("5");
    }

    private void testHold(String junitVersion) throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertTrue(2 < getLinesCount(report));
    }

    @Test
    public void testHoldIterationsJUnit4() throws Exception {
        testHoldIterations("4");
    }

    @Test
    public void testHoldIterationsJUnit5() throws Exception {
        testHoldIterations("5");
    }

    private void testHoldIterations(String junitVersion) throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertEquals(1, getLinesCount(report));
    }

    @Test
    public void testRunIncludeCategoriesJUnit4() throws Exception {
        testRunIncludeCategories("4");
    }

    @Test
    public void testRunIncludeCategoriesJUnit5() throws Exception {
        testRunIncludeCategories("5");
    }

    private void testRunIncludeCategories(String junitVersion) throws Exception {
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
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
    public void testRunExcludeCategoriesJUnit4() throws Exception {
        testRunExcludeCategories("4");
    }

    @Test
    public void testRunExcludeCategoriesJUnit5() throws Exception {
        testRunExcludeCategories("5");
    }

    private void testRunExcludeCategories(String junitVersion) throws Exception {
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
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
    public void testRunIncludeAndExcludeCategoriesJUnit4() throws Exception {
        testRunIncludeAndExcludeCategories("4");
    }

    @Test
    public void testRunIncludeAndExcludeCategoriesJUnit5() throws Exception {
        testRunIncludeAndExcludeCategories("5");
    }

    private void testRunIncludeAndExcludeCategories(String junitVersion) throws Exception {
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
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 1, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
    }

    @Test
    public void testRunIncludeAllCategoriesJUnit4() throws Exception {
        testRunIncludeAllCategories("4");
    }

    @Test
    public void testRunIncludeAllCategoriesJUnit5() throws Exception {
        testRunIncludeAllCategories("5");
    }

    private void testRunIncludeAllCategories(String junitVersion) throws Exception {
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
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
    public void testRunExcludeAllCategoriesJUnit4() throws Exception {
        testRunExcludeAllCategories("4");
    }

    @Test
    public void testRunExcludeAllCategoriesJUnit5() throws Exception {
        testRunExcludeAllCategories("5");
    }

    private void testRunExcludeAllCategories(String junitVersion) throws Exception {
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
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
    public void testRunItemsJUnit4() throws Exception {
        testRunItems("4");
    }

    @Test
    public void testRunItemsJUnit5() throws Exception {
        testRunItems("5");
    }

    private void testRunItems(String junitVersion) throws Exception {
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
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
    public void testRunAllJUnit4() throws Exception {
        testRunAll("4");
    }

    @Test
    public void testRunAllJUnit5() throws Exception {
        testRunAll("5");
    }

    private void testRunAll(String junitVersion) throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

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

    @Test
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

    @Test
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

    @Test
    public void testConcurrency() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(0));
        props.setProperty(CustomRunner.CONCURRENCY, String.valueOf(10));
        props.setProperty(CustomRunner.RAMP_UP, String.valueOf(6));
        props.setProperty(CustomRunner.STEPS, String.valueOf(2));

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        assertTrue(10000 < getLinesCount(report));
    }

    @Test
    public void testRunWithoutArgs() {
        try {
            CustomRunner.main(new String[0]);
            fail("Cannot work without path to properties file");
        } catch (Exception e) {
            assertEquals("Usage requires 1 parameter, containing path to properties file", e.getMessage());
        }
    }

    @Test
    public void testNoClasses() throws IOException {
        URL res = Thread.currentThread().getContextClassLoader().getResource("empty.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};

        try {
            CustomRunner.main(args);
            fail("jar contains 0 Test classes, can not continue");
        } catch (Exception e) {
            assertTrue(e instanceof CustomRunnerException);
            assertEquals("Nothing to test", e.getMessage());
        }
    }
}