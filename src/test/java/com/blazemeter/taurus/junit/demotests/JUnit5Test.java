package com.blazemeter.taurus.junit.demotests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JUnit5Test {
    @Test
    void testJUnit5Method() {
        Assertions.assertEquals("1", "1");
    }
}
