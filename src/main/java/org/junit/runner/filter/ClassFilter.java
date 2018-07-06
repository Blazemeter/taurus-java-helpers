package org.junit.runner.filter;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class ClassFilter extends Filter {

    private final Description description;

    public ClassFilter(Description description) {
        this.description = description;
    }

    @Override
    public boolean shouldRun(Description description) {
        return this.description.getTestClass().equals(description.getTestClass());
    }

    @Override
    public String describe() {
        return description.getDisplayName();
    }
}
