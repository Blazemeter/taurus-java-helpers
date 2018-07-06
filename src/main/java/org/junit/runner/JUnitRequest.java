package org.junit.runner;

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


    public static Request createSuiteRequest(String testSuite) {
        String[] testCases = testSuite.split(",");
        final List<Filter> filters = new ArrayList<>();
        final List<Class> classes = new ArrayList<>();

        for (String testCase : testCases) {
            try {
                if (testCase.contains("#")) {
                    String[] classAndMethod = testCase.split("#");

                    Class cls = Class.forName(classAndMethod[0]);
                    classes.add(cls);

                    Description description = Description.createTestDescription(cls, classAndMethod[1]);
                    filters.add(Filter.matchMethodDescription(description));
                } else {
                    Class cls = Class.forName(testCase);
                    classes.add(cls);

                    Description description = Description.createSuiteDescription(cls);
                    filters.add(new ClassFilter(description));
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                log.log(Level.WARNING, "Class not found", e);
            }
        }

        return Request.classes(classes.toArray(new Class[0])).filterWith(new OrFilter(filters));
    }

}
