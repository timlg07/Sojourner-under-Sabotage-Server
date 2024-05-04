import org.junit.Test;
import static org.junit.Assert.*;

/* For assertions you can use:
 - assertTrue
 - assertFalse
 - assertEquals
 - assertNotEquals
 - assertArrayEquals
 - assertThrows
 - assertNull
 - assertNotNull
 */

public class DemoTest {

    @Test
    public void hiddenTest1() {
        assertEquals(4, Demo.add(2, 2));
    }

    @Test
    public void hiddenTest2() {
        assertEquals(9, Demo.add(5, 4));
    }

    @Test
    public void hiddenTest3() {
        assertEquals(16, Demo.add(6, 10));
    }

}
