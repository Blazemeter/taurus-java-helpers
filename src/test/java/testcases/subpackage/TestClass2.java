package testcases.subpackage;

import categories.CategoryB;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

public class TestClass2 extends TestCase {

    public void test1() {
        int i = 0;
        i++;
        i--;
//        System.out.println("test 1");
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals("s", "s");

    }

    @Category(CategoryB.class)
    public void test2() {
        int i = 0;
        i++;
        i--;
//        System.out.println("test 2");
    }
}
