package testcases;

import categories.CategoryA;
import categories.CategoryB;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({CategoryA.class, CategoryB.class})
public class TestClass4 {
    @Test
    public void m1() {
        int i = 0;
        i++;
        i--;
//        System.out.println("m 1");
    }

    @Test
    public void m2() {
        int i = 0;
        i++;
        i--;
//        System.out.println("m 2");
    }
}
