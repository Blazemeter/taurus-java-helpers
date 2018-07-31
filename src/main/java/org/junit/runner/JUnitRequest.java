package org.junit.runner;

import com.blazemeter.taurus.classpath.ClasspathScanner;
import com.blazemeter.taurus.junit.exception.CustomRunnerException;
import org.junit.runner.filter.ClassFilter;
import org.junit.runner.filter.OrFilter;
import org.junit.runner.manipulation.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JUnitRequest {
    private static final Logger log = Logger.getLogger(JUnitRequest.class.getName());

    public static Request createCategoryRequest(String[] args) {
        JUnitCommandLineParseResult parse = JUnitCommandLineParseResult.parse(args);
        return parse.createRequest(Computer.serial());
    }

    public static Request createItemsRequest(String runItems, ClasspathScanner classpathScanner) {
        String[] testCases = runItems.split(",");
        final List<Filter> filters = new ArrayList<>();
        final List<Class> classes = new ArrayList<>();

        for (String testCase : testCases) {
            try {
                if (testCase.contains("#")) {
                    String[] classAndMethod = testCase.split("#");

                    Class<?> cls = classpathScanner.getClass(classAndMethod[0]);
                    classes.add(cls);

                    checkMethod(cls, classAndMethod[1]);

                    Description description = Description.createTestDescription(cls, classAndMethod[1]);
                    filters.add(Filter.matchMethodDescription(description));
                } else {
                    Class cls = classpathScanner.getClass(testCase);
                    classes.add(cls);

                    Description description = Description.createSuiteDescription(cls);
                    filters.add(new ClassFilter(description));
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                log.log(Level.SEVERE, "Class not found: " + testCase, e);
                throw new CustomRunnerException("Class not found: " + testCase, e);
            } catch (NoSuchMethodException e) {
                log.log(Level.SEVERE, "Method not found: " + testCase, e);
                throw new CustomRunnerException("Method not found: " + testCase, e);
            }
        }

        return Request.classes(classes.toArray(new Class[0])).filterWith(new OrFilter(filters));
    }

    public static void checkMethod(Class<?> cls, String methodName) throws NoSuchMethodException {
        cls.getDeclaredMethod(methodName);
    }

}
