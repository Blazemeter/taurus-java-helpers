package com.blazemeter.taurus.reporting;

import categories.TestCategory;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.math.BigDecimal;

@Category(TestCategory.class)
public class SampleTest extends TestCase {

    public void test() {
        long t1 = System.currentTimeMillis();
        Sample sample = new Sample();
        long t2 = System.currentTimeMillis();

        assertTrue(t2 >= sample.getStartTime());
        assertTrue(t1 <= sample.getStartTime());

        long start = sample.getStartTime();
        assertEquals(BigDecimal.valueOf(start, 3), sample.getStartTimeInSec());

        assertTrue(sample.isSuccessful());
        assertFalse(sample.isSkipped());

        sample.setStatus(Sample.STATUS_FAILED);
        assertFalse(sample.isSuccessful());
        assertFalse(sample.isSkipped());

        sample.setStatus(Sample.STATUS_SKIPPED);
        assertFalse(sample.isSuccessful());
        assertTrue(sample.isSkipped());

        sample.setActiveThreads(3);
        assertEquals(3, sample.getActiveThreads());

        sample.setSuite(null);
        assertEquals("", sample.getSuite());
        sample.setSuite("TestSuite");
        assertEquals("TestSuite", sample.getSuite());

        sample.setDescription(null);
        assertEquals("", sample.getDescription());
        sample.setDescription("desc");
        assertEquals("desc", sample.getDescription());

        sample.setFullName(null);
        assertEquals("", sample.getFullName());
        sample.setFullName("fullName");
        assertEquals("fullName", sample.getFullName());

        sample.setFile(null);
        assertEquals("", sample.getFile());
        sample.setFile("file");
        assertEquals("file", sample.getFile());

        sample.setErrorMessage(null);
        assertNull(sample.getErrorMessage());
        sample.setErrorMessage("msg");
        assertEquals("msg", sample.getErrorMessage());

        sample.setErrorTrace(null);
        assertNull(sample.getErrorTrace());
        sample.setErrorTrace("trace");
        assertEquals("trace", sample.getErrorTrace());

        sample.setLabel("label");
        assertEquals("label", sample.getLabel());

        sample.setDuration(5000);
        assertEquals(5000, sample.getDuration());
        assertEquals(5, sample.getDurationInSec(), 0.001);

        assertEquals(start + ": label - SKIPPED", sample.toString());
        assertEquals(Sample.STATUS_SKIPPED, sample.getStatus());

        sample.setStartTime(999);
        assertEquals(999, sample.getStartTime());
    }
}