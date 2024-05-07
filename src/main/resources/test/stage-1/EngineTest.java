import java.util.*;
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

public class EngineTest {

    @Test
    public void hiddenTest1() {
        Engine engine = new Engine();
        assertEquals(0, engine.getO2(0), 0.001);
    }

    @Test
    public void hiddenTest2() {
        Engine engine = new Engine();
        assertEquals(3.5454545, engine.getO2(1), 0.001);
    }

    @Test
    public void hiddenTest3() {
        Engine engine = new Engine();
        assertEquals(24.8181818, engine.getO2(7), 0.001);
    }

    @Test
    public void hiddenTest4() {
        Engine engine = new Engine();
        assertEquals(1.77272727, engine.getO2(0.5), 0.001);
    }

    @Test
    public void testEngineShutdown() {
        Engine engine = new Engine();
        engine.shutdown();
        assertEquals(0, engine.getO2(8), 0.001);
    }
}
