package testcases.subpackage;

import categories.CategoryA;
import categories.CategoryB;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class TestClass3 {
    @Test
    public void method1() {
//
        int i = 0;
        i++;
        i--;
//        System.out.println("method 1");
//        throw new RuntimeException("oopps");
    }

    @Category({CategoryA.class, CategoryB.class})
    @Test
    public void method2() {
        int i = 0;
        i++;
        i--;
//        System.out.println("method 2");
    }
}
