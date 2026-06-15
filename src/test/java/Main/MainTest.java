package Main;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    @Test
    void dummyTest() {
        var a = 1;
        var b = 5;
        var c = a + b;
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + c);
        System.out.println("Dummy test executed");
        assertEquals(6, c);
        assertTrue(true);
    }
}
