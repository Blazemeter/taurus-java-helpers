package com.blazemeter.taurus.junit;

public interface ThreadCounter {
    void incrementActiveThreads();

    void decrementActiveThreads();

    int getActiveThreads();
}
