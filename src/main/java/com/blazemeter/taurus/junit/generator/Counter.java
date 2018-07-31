package com.blazemeter.taurus.junit.generator;

import com.blazemeter.taurus.junit.api.ThreadCounter;

public class Counter implements ThreadCounter {

    private int activeThreads = 0;

    public synchronized void incrementActiveThreads() {
        activeThreads++;
    }

    public synchronized void decrementActiveThreads() {
        activeThreads--;
    }

    public synchronized int getActiveThreads() {
        return activeThreads;
    }
}
