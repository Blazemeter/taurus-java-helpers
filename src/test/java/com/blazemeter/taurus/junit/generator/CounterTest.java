package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.ThreadCounter;
import junit.framework.TestCase;

public class CounterTest extends TestCase {

    public void testActiveThreads() {

        ThreadCounter counter = new Counter();
        counter.incrementActiveThreads();

        assertEquals(1, counter.getActiveThreads());

        counter.decrementActiveThreads();
        assertEquals(0, counter.getActiveThreads());
    }

}