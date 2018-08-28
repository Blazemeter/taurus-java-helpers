package org.junit.runner.filter;


import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;
import org.junit.runner.Description;

@Category(TestCategory.class)
public class ClassFilterTest extends TestCase {

    public void testFlow() {
        Description description = Description.createSuiteDescription(ClassFilterTest.class);
        ClassFilter filter = new ClassFilter(description);
        assertTrue(filter.shouldRun(description));
        assertFalse(filter.shouldRun(Description.createSuiteDescription(ClassFilter.class)));
        assertEquals(ClassFilterTest.class.getName(), filter.describe());
    }
}