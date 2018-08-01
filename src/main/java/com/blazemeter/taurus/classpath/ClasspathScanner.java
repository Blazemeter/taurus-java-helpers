package com.blazemeter.taurus.classpath;

import org.junit.internal.Classes;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClasspathScanner {
    private static final Logger log = Logger.getLogger(ClasspathScanner.class.getName());

    private Filter filter;

    public ClasspathScanner(Filter filter) {
        this.filter = filter;
    }

    public List<Class> getAllTestClasses(ClassLoader cl) {
        List<Class> classFiles = new ArrayList<>();
        List<File> classLocations = getClassLocationsForCurrentClasspath();
        for (File file : classLocations) {
            classFiles.addAll(getClassesFromPath(file, cl));
        }
        log.info("Found " + classFiles.size() + " classes");
        return classFiles;
    }

    public Package getPackage(String packageName) {
        return Package.getPackage(packageName);
    }

    public Class getClass(String className) throws ClassNotFoundException {
        return Classes.getClass(className);
    }

    private List<Class> getClassesFromPath(File path, ClassLoader cl) { //TODO : make ClassLoader field
        if (path.isDirectory()) {
            return getClassesFromDirectory(path, cl);
        } else {
            return getClassesFromJarFile(path, cl);
        }
    }

    private String fromFileToClassName(final String fileName) {
        String className = fileName.substring(0, fileName.length() - ".class".length());
        return className.replace('/', '.');
    }

    private List<Class> getClassesFromJarFile(File path, ClassLoader cl) {
        List<Class> classes = new ArrayList<>();
        log.fine("Getting classes for " + path);

        try {
            if (path.canRead()) {
                JarFile jar = new JarFile(path);
                Enumeration<JarEntry> en = jar.entries();
                while (en.hasMoreElements()) {
                    JarEntry entry = en.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = fromFileToClassName(entry.getName());
                        processClass(classes, className, cl);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read classes from jar file: " + path, e);
        }

        return classes;
    }

    private List<Class> getClassesFromDirectory(File path, ClassLoader cl) {
        List<Class> classes = new ArrayList<>();
        log.fine("Getting classes for " + path);

        List<File> jarFiles = listFiles(path, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        }, false);
        for (File file : jarFiles) {
            classes.addAll(getClassesFromJarFile(file, cl));
        }

        List<File> classFiles = listFiles(path, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        }, true);

        int substringBeginIndex = path.getAbsolutePath().length() + 1;
        for (File classfile : classFiles) {
            String className = classfile.getAbsolutePath().substring(substringBeginIndex);
            className = fromFileToClassName(className);
            processClass(classes, className, cl);
        }

        return classes;
    }

    private List<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
        List<File> files = new ArrayList<>();
        File[] entries = directory.listFiles();

        if (entries != null) {
            for (File entry : entries) {
                if (filter == null || filter.accept(directory, entry.getName())) {
                    files.add(entry);
                }

                if (recurse && entry.isDirectory()) {
                    files.addAll(listFiles(entry, filter, true));
                }
            }
        }
        return files;
    }

    public List<File> getClassLocationsForCurrentClasspath() {
        List<File> urls = new ArrayList<>();
        String javaClassPath = getClassPath();
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(File.pathSeparator)) {
                urls.add(new File(path));
            }
        }
        return urls;
    }

    protected String getClassPath() {
        return System.getProperty("java.class.path");
    }

    protected void processClass(List<Class> classes, String className, ClassLoader cl) {
        try {
            Class c = cl.loadClass(className);
            if (filter.shouldAdd(c)) {
                classes.add(c);
            }
        } catch (Throwable e) {
            log.log(Level.SEVERE, "Failed to process class: " + className);
        }
    }
}
