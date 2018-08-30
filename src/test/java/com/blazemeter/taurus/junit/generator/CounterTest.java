package com.blazemeter.taurus.junit.generator;

import categories.TestCategory;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(TestCategory.class)
public class CounterTest extends TestCase {

    public void testActiveThreads() {

        ThreadCounter counter = new Counter();
        counter.incrementActiveThreads();

        assertEquals(1, counter.getActiveThreads());

        counter.decrementActiveThreads();
        assertEquals(0, counter.getActiveThreads());
    }

}