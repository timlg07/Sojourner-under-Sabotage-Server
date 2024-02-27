import org.junit.Test;
import static org.junit.Assert.*;

public class DemoTest {

    @Test
    public void test() {
        assertEquals(4, Demo.add(2, 2));
    }

    @Test
    public void test2() {
        assertEquals(9, Demo.add(5, 4));
    }

}
