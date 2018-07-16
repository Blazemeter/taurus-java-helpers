package com.blazemeter.taurus.junit5;

import com.blazemeter.taurus.junit.CustomRunner;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;

import static com.blazemeter.taurus.junit.CustomRunnerTest.getLinesCount;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.*;

public class JUnit5RunnerTest {

    @Test
    public void testFlow() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

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
    public void testIncludeAndExcludePackages() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "testcases");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "testcases.subpackage");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    @Test
    public void testIncludePackages() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "testcases.subpackage");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
    }

    @Test
    public void testExcludePackages() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "testcases.subpackage");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    @Test
    public void testRunPackage() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackage");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
    }


    @Test
    public void testRunClass() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackage.TestClass2");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 2, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
    }

    @Test
    public void testRunMethod() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackage.TestClass2#test2");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 1, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
    }

    @Test
    public void testRunAllItems() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1,testcases.TestClass4#m1,testcases.subpackage");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        CustomRunner.main(args);

        String fileToString = readFileToString(report);
        assertEquals(fileToString, 7, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
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
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

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
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class or Package not found: testcases.TestClass77", e.getMessage());
        }
    }

    @Test
    public void testClassNotFound1() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass12#test2");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class not found: testcases.TestClass12#test2", e.getMessage());
        }
    }

    @Test
    public void testPackageNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackagE");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class or Package not found: testcases.subpackagE", e.getMessage());
        }
    }

    @Test
    public void testPackageFilterNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "testcases.subpackagE");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Filter Class or Package not found: testcases.subpackagE", e.getMessage());
        }
    }

    @Test
    public void testClassFilterNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.TARGET_PREFIX + "jar", res.getPath());
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryE");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        try {
            CustomRunner.main(args);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Filter Class or Package not found: categories.CategoryE", e.getMessage());
        }
    }
}