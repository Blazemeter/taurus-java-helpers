package com.blazemeter.taurus.junit.demotests;

import categories.TestCategory;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Category(TestCategory.class)
public class JUnit5Test {
    @Test
    void testJUnit5Method() {
        Assertions.assertEquals("1", "1");
    }
}
