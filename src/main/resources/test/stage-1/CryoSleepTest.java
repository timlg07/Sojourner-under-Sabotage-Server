import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

public class CryoSleepTest {

    @Test
    public void testInitiallyFrozen() {
        CryoSleep cryo = new CryoSleep(6);
        assertTrue(cryo.isFrozen());
    }

    @Test
    public void testInitiallyNotFrozen() {
        CryoSleep cryo = new CryoSleep(0);
        assertFalse(cryo.isFrozen());
    }

    @Test
    public void testStillFrozen1DayBefore() {
        CryoSleep cryo = new CryoSleep(3);
        cryo.dayPassed();
        cryo.dayPassed();
        assertTrue(cryo.isFrozen());
    }

    @Test
    public void testWakeUp() {
        CryoSleep cryo = new CryoSleep(3);
        cryo.dayPassed();
        cryo.dayPassed();
        cryo.dayPassed();
        assertFalse(cryo.isFrozen());
    }

}
