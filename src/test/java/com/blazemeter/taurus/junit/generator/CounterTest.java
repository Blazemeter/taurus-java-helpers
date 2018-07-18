package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.ThreadCounter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CounterTest {

    @Test
    public void testActiveThreads() throws Exception {

        ThreadCounter counter = new Counter();
        counter.incrementActiveThreads();

        assertEquals(1, counter.getActiveThreads());

        counter.decrementActiveThreads();
        assertEquals(0, counter.getActiveThreads());
    }

}