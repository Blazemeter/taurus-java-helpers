package com.blazemeter.taurus.utils;

import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;


@Category(TestCategory.class)
public class UtilsTest  extends TestCase {

    public void testEscape() {
        String input = "Hello, \r\n How are you, \"man\"?";
        String output = Utils.escapeCSV(input);
        assertEquals("\"Hello, \r\n" +
                " How are you, \"\"man\"\"?\"",output);
    }

}