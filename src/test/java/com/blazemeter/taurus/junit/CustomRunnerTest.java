package com.blazemeter.taurus.junit;


import com.blazemeter.taurus.classpath.ClasspathScanner;
import com.blazemeter.taurus.junit.api.JUnitRunner;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import com.blazemeter.taurus.junit.generator.Counter;
import com.blazemeter.taurus.reporting.TaurusReporter;
import com.blazemeter.taurus.junit.runner.junit4.JUnit4Runner;
import com.blazemeter.taurus.junit.runner.junit5.JUnit5Runner;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;


public class CustomRunnerTest extends TestCase {


    @Override
    protected void setUp() throws Exception {
        //TODO: BAD BAD BAD HACK
        // we should remove it and rewrite tests where we use Package.getPackage() and Class.forName() in JUnit5Runner
        addURL(Thread.currentThread().getContextClassLoader().getResource("junit-test-1.1.jar"));
    }

    private static void addURL(URL url) throws ReflectiveOperationException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(systemClassLoader, url);
    }

    private static JUnitRunner createRunner(String junitVersion, String classpath) {
        return "5".equals(junitVersion) ?
                createJUnit5Runner(classpath) :
                createJUnit4Runner(classpath);
    }

    private static JUnitRunner createJUnit5Runner(String classpath) {
        return new JUnit5Runner() {
            @Override
            protected ClassLoader getClassLoader() {
                return TestClassLoader.getTestClassLoaderWithSystemCL();
            }

            @Override
            protected ClasspathScanner createClasspathScanner() {
                return new ClasspathScanner(new ClassFilter()) {
                    @Override
                    protected String getClassPath() {
                        return classpath;
                    }

                    @Override
                    public Class getClass(String className) throws ClassNotFoundException {
                        URLClassLoader cl;
                        try {
                            cl = new URLClassLoader(new URL[]{new URL("jar:file:" + classpath + "!/")});
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        return Class.forName(className, true, cl);
                    }
                };
            }
        };
    }

    private static JUnitRunner createJUnit4Runner(String classpath) {
        return new JUnit4Runner() {
            @Override
            protected ClassLoader getClassLoader() {
                return TestClassLoader.getTestClassLoaderWithSystemCL();
            }

            @Override
            protected ClasspathScanner createClasspathScanner() {
                return new ClasspathScanner(new ClassFilter()) {
                    @Override
                    protected String getClassPath() {
                        return classpath;
                    }
                };
            }
        };
    }

    public static void process(String junitVersion, String classpath, Properties properties, File report) throws Exception {
        TaurusReporter reporter = new TaurusReporter(report.getAbsolutePath());
        JUnitRunner runner = createRunner(junitVersion, classpath);
        Object request = runner.createRequest(properties);
        runner.executeRequest(request, reporter, new Counter());
        reporter.close();
    }


    public static int getLinesCount(File log) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(log));
        reader.skip(Long.MAX_VALUE);
        reader.close();
        return reader.getLineNumber();
    }

    public static String readFileToString(File log) throws IOException {
        return FileUtils.readFileToString(log);
    }

    public void testPassProperties() throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("dummyJUnit.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.REPORT_FILE, report.getAbsolutePath());
        props.setProperty("myProperty", "myValue");
        props.put(CustomRunner.RUN_ITEMS, EmptyTestClass.class.getName());

        File propsFile = File.createTempFile("runner", ".properties");
        propsFile.deleteOnExit();
        props.store(new FileWriter(propsFile), "test");

        String[] args = {propsFile.getAbsolutePath()};
        System.clearProperty("myProperty");
        CustomRunner.main(args);
        assertEquals("myValue", System.getProperty("myProperty"));
        assertEquals(1, getLinesCount(report));
    }

    public void testRunIncludeCategoriesJUnit4() throws Exception {
        testRunIncludeCategories("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryA");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunExcludeCategoriesJUnit4() throws Exception {
        testRunExcludeCategories("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "categories.CategoryB");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
    }

    public void testRunIncludeAndExcludeCategoriesJUnit4() throws Exception {
        testRunIncludeAndExcludeCategories("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryA");
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "categories.CategoryB");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


        String fileToString = readFileToString(report);

        assertEquals(fileToString, 1, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
    }

    public void testRunIncludeAllCategoriesJUnit4() throws Exception {
        testRunIncludeAllCategories("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.INCLUDE_CATEGORY, "categories.CategoryA,categories.CategoryB");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


        String fileToString = readFileToString(report);

        assertEquals(fileToString, 5, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunExcludeAllCategoriesJUnit4() throws Exception {
        testRunExcludeAllCategories("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.EXCLUDE_CATEGORY, "categories.CategoryA,categories.CategoryB");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


        String fileToString = readFileToString(report);

        assertEquals(fileToString, 3, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass3.method1"));
    }

    public void testRunItemsJUnit4() throws Exception {
        testRunItems("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1#flow1,testcases.subpackage.TestClass2#test2,testcases.TestClass4");
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


        String fileToString = readFileToString(report);

        assertEquals(fileToString, 4, getLinesCount(report));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass1.flow1"));
        assertTrue(fileToString, fileToString.contains("testcases.subpackage.TestClass2.test2"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m1"));
        assertTrue(fileToString, fileToString.contains("testcases.TestClass4.m2"));
    }

    public void testRunAllJUnit4() throws Exception {
        testRunAll("4");
    }

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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        process(junitVersion, res.getPath(), props, report);


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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass1#flow3,testcases.TestClass2#test2");


        try {
            process("4", res.getPath(), props, report);
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
        props.setProperty(CustomRunner.HOLD, String.valueOf(5));
        props.setProperty(CustomRunner.ITERATIONS, String.valueOf(1));
        props.setProperty(CustomRunner.RUN_ITEMS, "testcases.TestClass77,testcases.TestClass2#test2");

        try {
            process("4", res.getPath(), props, report);
            fail("Should be ClassNotFoundException");
        } catch (Exception e) {
            assertEquals("Class not found: testcases.TestClass77", e.getMessage());
        }
    }

    public void testRunWithoutArgs() {
        try {
            CustomRunner.main(new String[0]);
            fail("Cannot work without path to properties file");
        } catch (Exception e) {
            assertEquals("Usage requires 1 parameter, containing path to properties file", e.getMessage());
        }
    }

    public void testNoClassesJunit5() throws Exception {
        testNoClasses("5");
    }

    public void testNoClassesJunit4() throws Exception {
        testNoClasses("4");
    }

    private void testNoClasses(String junitVersion) throws Exception {
        File report = File.createTempFile("report", ".ldjson");
        report.deleteOnExit();

        URL res = Thread.currentThread().getContextClassLoader().getResource("empty.jar");
        assert res != null;

        Properties props = new Properties();
        props.setProperty(CustomRunner.JUNIT_VERSION, junitVersion);

        try {
            process(junitVersion, res.getPath(), props, report);
            fail("jar contains 0 Test classes, can not continue");
        } catch (CustomRunnerException e) {
            assertEquals("Nothing to test", e.getMessage());
        }
    }
}