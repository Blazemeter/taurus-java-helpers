package com.blazemeter.taurus.junit.runner.junit5;

import categories.TestCategory;
import com.blazemeter.taurus.junit.CustomRunner;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Properties;

import static com.blazemeter.taurus.junit.CustomRunnerTest.EXCLUDE_TEST_FOLDERS;
import static com.blazemeter.taurus.junit.CustomRunnerTest.getLinesCount;
import static com.blazemeter.taurus.junit.CustomRunnerTest.process;
import static org.apache.commons.io.FileUtils.readFileToString;

@Category(TestCategory.class)
public class JUnit5RunnerTest extends TestCase {

    public void testFlow() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, EXCLUDE_TEST_FOLDERS);

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 9, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestCase5.testJUnit5Method"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testIncludeAndExcludePackages() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "testcases");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "testcases.subpackage," + EXCLUDE_TEST_FOLDERS);

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testIncludePackages() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "testcases.subpackage");

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 5, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestCase5.testJUnit5Method"));
    }

    public void testExcludePackages() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "testcases.subpackage," + EXCLUDE_TEST_FOLDERS);

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunPackage() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackage");

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 5, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestCase5.testJUnit5Method"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
    }

    public void testRunClass() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackage.TestClass2");

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 2, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
    }

    public void testRunMethod() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackage.TestClass2#test2");

        process("5", props, report);

        String fileToString = readFileToString(report);

        assertEquals(fileToString, 1, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
    }

    public void testRunAllItems() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1,testcases.TestClass4#m1,testcases.subpackage");

        process("5", props, report);

        String fileToString = readFileToString(report);
        assertEquals(fileToString, 8, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestCase5.testJUnit5Method"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
    }

    public void testMethodNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1#flow3,testcases.TestClass2#test2");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        try {
            process("5", props, report);
            fail("Should be NoSuchMethodException");
        } catch (Exception e) {
            assertEquals("Method not found: testcases.TestClass1#flow3", e.getMessage());
        }
    }

    public void testClassNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass77,testcases.TestClass2#test2");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        try {
            process("5", props, report);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class or Package not found: testcases.TestClass77", e.getMessage());
        }
    }

    public void testClassNotFound1() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass12#test2");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        try {
            process("5", props, report);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class not found: testcases.TestClass12#test2", e.getMessage());
        }
    }

    public void testPackageNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.subpackagE");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        try {
            process("5", props, report);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class or Package not found: testcases.subpackagE", e.getMessage());
        }
    }

    public void testPackageFilterNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "testcases.subpackagE");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        try {
            process("5", props, report);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Filter Class or Package not found: testcases.subpackagE", e.getMessage());
        }
    }

    public void testClassFilterNotFound() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryE");
        props.setProperty(CustomRunner.JUNIT_VERSION, "5");

        try {
            process("5", props, report);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Filter Class or Package not found: categories.CategoryE", e.getMessage());
        }
    }

    public void testGetClassLoader() {
        JUnit5Runner runner = new JUnit5Runner();
        assertEquals(ClassLoader.getSystemClassLoader(), runner.getClassLoader());
    }
}