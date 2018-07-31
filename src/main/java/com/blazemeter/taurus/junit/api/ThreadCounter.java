package com.blazemeter.taurus.junit.api;

public interface ThreadCounter {
    void incrementActiveThreads();

    void decrementActiveThreads();

    int getActiveThreads();
}
