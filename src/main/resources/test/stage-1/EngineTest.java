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

    private Engine engine;

    @org.junit.Before
    public void setUp() {
        engine = new Engine();
    }

    @Test
    public void hiddenTest1() {
        assertEquals(0, engine.getO2(0), 0.001);
    }

    @Test
    public void hiddenTest2() {
        assertEquals(3.5454545, engine.getO2(1), 0.001);
    }

    @Test
    public void hiddenTest3() {
        assertEquals(24.8181818, engine.getO2(7), 0.001);
    }

    @Test
    public void hiddenTest4() {
        assertEquals(1.77272727, engine.getO2(0.5), 0.001);
    }

/*
    @ParameterizedTest
    @ValueSource(doubles = {0, 0.5, 1, 1.3333, 2, 3, 7.8999, 800, 1337})
    public void testEngineShutdown(double ch4) {
        engine.shutdown();
        assertEquals(0, engine.getO2(ch4), 0.001);
    }
*/

    @Test
    public void testEngineShutdown() {
        engine.shutdown();
        assertEquals(0, engine.getO2(8), 0.001);
    }
}
