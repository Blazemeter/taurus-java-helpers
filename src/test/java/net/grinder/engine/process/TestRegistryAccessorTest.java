package net.grinder.engine.process;

import junit.framework.TestCase;
import net.grinder.script.Grinder;
import net.grinder.script.Test;

public class TestRegistryAccessorTest extends TestCase {
    public void testNewTestsEmpty() {
        Grinder.grinder = TestRegistryAccessor.getDummyScriptContext();
        assertEquals(0, TestRegistryAccessor.getNewTests().size());
        new Test(1, "label");
        assertEquals(1, TestRegistryAccessor.getNewTests().size());
        assertEquals(0, TestRegistryAccessor.getNewTests().size());
    }

}