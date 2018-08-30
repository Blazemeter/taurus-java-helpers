package com.blazemeter.taurus.grinder;

import categories.TestCategory;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;
import junit.framework.TestCase;
import net.grinder.engine.process.TestRegistryAccessor;
import net.grinder.script.Grinder;
import net.grinder.util.logback.BufferedEchoMessageEncoder;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

@Category(TestCategory.class)
public class TaurusAppenderTest extends TestCase {
    public void testWriteOut() throws Exception {
        Grinder.grinder = TestRegistryAccessor.getDummyScriptContext();

        new net.grinder.script.Test(1, "label");

        TaurusAppender appender = new TaurusAppender();
        BufferedEchoMessageEncoder encoder = new BufferedEchoMessageEncoder();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encoder.init(os);
        appender.setEncoder(encoder);
        LoggingEvent event = new LoggingEvent();
        event.setLoggerContextRemoteView(new LoggerContextVO("", new HashMap<String, String>(), 0));
        event.getLoggerContextVO().getPropertyMap().put("WORKER_NAME", "worker1");
        event.setLoggerName("data");
        event.setMessage("Tada!");
        event.setCallerData(new StackTraceElement[0]);
        appender.setOutputStream(os);
        appender.start();
        appender.writeOut(event);
        appender.stop();

        assertEquals("Tada!\n", os.toString());
    }

}