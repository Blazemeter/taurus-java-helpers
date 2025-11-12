package com.blazemeter.taurus.junit.runner.junit5;

import com.blazemeter.taurus.junit.CustomListener;
import com.blazemeter.taurus.junit.api.Reporter;
import com.blazemeter.taurus.junit.api.ThreadCounter;
import com.blazemeter.taurus.reporting.Sample;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;
import java.util.logging.Logger;

public class JUnit5Listener extends CustomListener implements TestExecutionListener {
    private static final Logger log = Logger.getLogger(JUnit5Listener.class.getName());

    public JUnit5Listener(Reporter reporter, ThreadCounter counter) {
        super(reporter, counter);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if (isVerbose()) {
            log.info("Test Plan Started");
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (isVerbose()) {
            log.info("Test Plan Finished, successful=" + (getFailedCount() == 0) + ", run count=" + (getTestCount() - getSkippedCount()));
        }
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            startSample(testIdentifier);
        }
    }

    protected void startSample(TestIdentifier testIdentifier) {
        Optional<TestSource> source = testIdentifier.getSource();
        if (source.isPresent()) {
            TestSource testSource = testIdentifier.getSource().get();
            if (testSource instanceof MethodSource) {
                MethodSource src = (MethodSource) testSource;
                String sampleName = getSampleName(src);
                startSample(sampleName, src.getClassName());
            } else {
                log.severe("Unsupported test source: " + testSource.getClass().getName());
                //TODO: other test source..
            }
        }
    }

    private String getSampleName(MethodSource src) {
        Class<?> javaClass = getJavaClass(src);
        Method method = StringUtils.isBlank(src.getMethodParameterTypes()) ? getJavaMethod(src, javaClass) : getJavaMethodWithParams(src, javaClass);
        DisplayName displayNameAnnotation = method.getDeclaredAnnotation(DisplayName.class);
        return (displayNameAnnotation != null) ? displayNameAnnotation.value() : src.getMethodName();
    }

    private Class<?> getJavaClass(MethodSource src) {
        return ReflectionUtils.loadClass(src.getClassName()).orElseThrow(
            () -> new PreconditionViolationException(String.format(
                "Could not load class [%s].", src.getClassName())));
    }

    private Method getJavaMethod(MethodSource src, Class<?> javaClass) {
        return ReflectionUtils.findMethod(javaClass, src.getMethodName()).orElseThrow(
            () -> new PreconditionViolationException(
                String.format("Could not find method with name [%s] in class [%s].", src.getMethodName(), javaClass.getName())));
    }

    private Method getJavaMethodWithParams(MethodSource src, Class<?> javaClass) {
        return ReflectionUtils.findMethod(javaClass, src.getMethodName(), src.getMethodParameterTypes()).orElseThrow(
            () -> new PreconditionViolationException(String.format(
                "Could not find method with name [%s] and parameter types [%s] in class [%s].",
                src.getMethodName(), src.getMethodParameterTypes(), javaClass.getName())));
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String status = getStatus(testExecutionResult.getStatus());
            Optional<Throwable> optional = testExecutionResult.getThrowable();
            if (optional.isPresent()) {
                Throwable throwable = optional.get();
                String exceptionName = throwable.getClass().getName();
                finishSample(getStatusFromThrowableType(throwable), exceptionName + ": " + throwable.getMessage(), throwable);
            } else {
                finishSample(status, null, null);
            }
        } else {
            //container or other -> we need to somehow handel errors in @BeforeAll/@AfterAll
            Optional<Throwable> optional = testExecutionResult.getThrowable();
            if (optional.isPresent()) {
                Throwable throwable = optional.get();
                String exceptionName = throwable.getClass().getName();
                if (isVerbose()) {
                    log.severe(String.format("failed %s(%s)", testIdentifier.getDisplayName(), "container"));
                }
                //we don't have sample here, so we create a dummy one to report the error
                pendingSample = new Sample();
                pendingSample.setLabel(testIdentifier.getDisplayName());
                pendingSample.setSuite("container");
                pendingSample.setFullName("container." + testIdentifier.getDisplayName());
                pendingSample.setContainerSample(true);
                finishSample(getStatusFromThrowableType(throwable), exceptionName + ": " + throwable.getMessage(), throwable);
            }
        }
    }

    protected String getStatusFromThrowableType(Throwable exception) {
        if (exception instanceof AssertionFailedError) {
            return Sample.STATUS_FAILED;
        }
        return Sample.STATUS_BROKEN;
    }

    protected String getStatus(TestExecutionResult.Status status) {
        switch (status) {
            case SUCCESSFUL:
                return Sample.STATUS_PASSED;
            case FAILED:
                if (isVerbose()) {
                    log.severe(String.format("failed %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                }
                return Sample.STATUS_FAILED;
            case ABORTED:
                if (isVerbose()) {
                    log.severe(String.format("aborted %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                }
                return Sample.STATUS_BROKEN;
                default:
                    if (isVerbose()) {
                        log.severe(String.format("failed %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
                    }
                    return Sample.STATUS_FAILED;
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        startSample(testIdentifier);
        if (isVerbose()) {
            log.warning(String.format("ignored %s(%s)", pendingSample.getLabel(), pendingSample.getSuite()));
        }
        finishSample(Sample.STATUS_SKIPPED, reason, null);
    }

}
