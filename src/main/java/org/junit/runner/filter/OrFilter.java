package org.junit.runner.filter;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.List;

public class OrFilter extends Filter {

    private final List<Filter> filters;

    public OrFilter(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public boolean shouldRun(Description description) {
        for (Filter f : filters) {
            if (f.shouldRun(description)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String describe() {
        StringBuilder builder = new StringBuilder();
        for (Filter f : filters) {
            builder.append(f.describe()).append(" or ");
        }
        return builder.substring(0, builder.length() - 3);
    }
}
