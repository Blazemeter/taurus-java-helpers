package testcases.subpackage;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TestCase6 {
    @Test
    @DisplayName("DisplayNameValue")
    void methodName() {
        Assertions.assertEquals("1", "1");
    }
}
