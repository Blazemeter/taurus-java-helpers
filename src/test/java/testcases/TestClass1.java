package testcases;

import categories.CategoryA;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class TestClass1 {

    @Ignore("hahaha")
    @Test
    public void flow1() {
//        System.out.println("flow 1");
        int i = 0;
        i++;
        i--;
    }

    @Category(CategoryA.class)
    @Test
    public void flow2() {
        int i = 0;
        i++;
        i--;
//        System.out.println("flow 2");
    }
}
