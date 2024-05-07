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

public class ReactorLogTest {

    @Test
    public void testInitialTemperature() {
        ReactorLog log = new ReactorLog(332);
        assertEquals(332, log.getCurrentTemperature());
    }

    @Test
    public void testLogTemperature() {
        ReactorLog log = new ReactorLog(300);
        log.logTemperature(298);
        assertEquals(298, log.getCurrentTemperature());
    }

    @Test
    public void testGetMaximumTemperature() {
        ReactorLog log = new ReactorLog(324);
        log.logTemperature(345);
        log.logTemperature(315);
        assertEquals(345, log.getMaximumTemperature());
    }

    @Test
    public void testLogsSinceMaxTemperature() {
        ReactorLog log = new ReactorLog(324);
        log.logTemperature(345);
        log.logTemperature(315);
        log.logTemperature(278);
        assertEquals(2, log.logsSinceMaxTemperature());
    }

    @Test
    public void testLogsSinceMaxTemperatureWithMaxTwice() {
        ReactorLog log = new ReactorLog(324);
        log.logTemperature(345);
        log.logTemperature(315);
        log.logTemperature(345);
        assertEquals(0, log.logsSinceMaxTemperature());
    }
}
