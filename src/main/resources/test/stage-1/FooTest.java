import org.junit.Test;
import static org.junit.Assert.*;

public class FooTest {

    @Test
    public void hiddenTest1() {
        assertEquals(4, Foo.add(2, 2));
    }

    @Test
    public void hiddenTest2() {
        assertEquals(9, Foo.add(5, 4));
    }

}
