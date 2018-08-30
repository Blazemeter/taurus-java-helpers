package com.blazemeter.taurus.classpath;

import categories.TestCategory;
import com.blazemeter.taurus.junit.demotests.EmptyTestClass;
import com.blazemeter.taurus.junit.demotests.JUnit3Test;
import com.blazemeter.taurus.junit.demotests.JUnit4Test;
import com.blazemeter.taurus.junit.demotests.JUnit5Test;
import com.blazemeter.taurus.junit.demotests.NoTestClass;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.List;

@Category(TestCategory.class)
public class ClasspathScannerTest extends TestCase {

    public void testFlow() {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter()) {
            @Override
            protected String getClassPath() {
                File classpathDir = new File(getClass().getResource("").getFile());
                return classpathDir.getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
            }
        };

        List<Class> allTestClasses = scanner.getAllTestClasses(ClassLoader.getSystemClassLoader());
        // here should be all test classes from Test folder and from jar in resources
        assertTrue(5 < allTestClasses.size());
    }

    public void testFromJar() {
        ClasspathScanner scanner = new ClasspathScanner(new TestFilter()) {
            @Override
            protected String getClassPath() {
                return getClass().getResource("/demo-tests.jar").getPath();
            }
        };
        List<Class> allTestClasses = scanner.getAllTestClasses(ClassLoader.getSystemClassLoader());
        // 5 test class + 1 interfaces (Test filter add all classes)
        assertEquals(6, allTestClasses.size());
        assertTrue(allTestClasses.contains(EmptyTestClass.class));
        assertTrue(allTestClasses.contains(JUnit3Test.class));
        assertTrue(allTestClasses.contains(JUnit4Test.class));
        assertTrue(allTestClasses.contains(JUnit5Test.class));
        assertTrue(allTestClasses.contains(NoTestClass.class));
        assertTrue(allTestClasses.contains(TestCategory.class));
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