package com.blazemeter.taurus.classpath;

import com.blazemeter.taurus.junit.TestClassLoader;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

public class ClasspathScannerTest extends TestCase {

    public void testFlow() {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter()) {
            @Override
            protected String getClassPath() {
                File classpathDir = new File(getClass().getResource("").getFile());
                return classpathDir.getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
            }
        };
        TestClassLoader cl = TestClassLoader.getTestClassLoaderWithSystemCL();

        List<Class> allTestClasses = scanner.getAllTestClasses(cl);
        // here should be all test classes from Test folder and from jar in resources
        assertTrue(5 < allTestClasses.size());
    }

    public void testFromJar() {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter()) {
            @Override
            protected String getClassPath() {
                return getClass().getResource("/junit-test-1.1.jar").getPath();
            }
        };
        TestClassLoader cl = TestClassLoader.getTestClassLoaderWithSystemCL();

        List<Class> allTestClasses = scanner.getAllTestClasses(cl);
        // 4 test class + 2 interfaces (Test filter add all classes)
        assertEquals(6, allTestClasses.size());
    }

    public void testGetClassPath() {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter());
        assertEquals(System.getProperty("java.class.path"), scanner.getClassPath());
    }

    public void testGetClass() throws ClassNotFoundException {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter());
        assertEquals(ClasspathScannerTest.class , scanner.getClass(ClasspathScannerTest.class.getName()));
    }

    public void testGetPackage() {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter());
        assertEquals(Package.getPackage("com.blazemeter"), scanner.getPackage("com.blazemeter"));
    }

    public static class TestFilter implements Filter {
        @Override
        public boolean shouldAdd(Class cls) {
            return true;
        }
    }
}