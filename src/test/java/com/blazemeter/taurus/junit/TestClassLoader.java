package com.blazemeter.taurus.junit;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class TestClassLoader extends URLClassLoader {
    public TestClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public TestClassLoader(URL[] urls) {
        super(urls);
    }

    public static URL[] getTestJars() {
        List<URL> urls = new ArrayList<>();
        urls.add(TestClassLoader.class.getResource("/junit-test-1.1.jar"));
        urls.add(TestClassLoader.class.getResource("/dummyJUnit.jar"));
        urls.add(TestClassLoader.class.getResource("/dummyTestNG.jar"));
        urls.add(TestClassLoader.class.getResource("/empty.jar"));
        return urls.toArray(new URL[0]);
    }

    public static TestClassLoader getTestClassLoaderWithSystemCL() {
        return new TestClassLoader(getTestJars(), ClassLoader.getSystemClassLoader());
    }

    public static TestClassLoader getTestClassLoaderWithoutSystemCL() {
        return new TestClassLoader(getTestJars());
    }
}
