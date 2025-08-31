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

public class GreenHouseTest {

    @Test
    public void testCannotPlantTwice() {
        GreenHouse greenhouse = new GreenHouse(3);
        greenhouse.plant(1);
        assertThrows(IllegalStateException.class, () -> greenhouse.plant(1));
    }

    @Test
    public void testCannotHarvestUnready() {
        GreenHouse greenhouse = new GreenHouse(3);
        greenhouse.plant(1);
        assertThrows(IllegalStateException.class, () -> greenhouse.harvest(1));
    }

    @Test
    public void testAutomatic() {
        GreenHouse greenhouse = new GreenHouse(3);
        greenhouse.setPlantInfo(new GreenHouse.Plant[] {
                GreenHouse.Plant.DEAD,
                GreenHouse.Plant.GROWING,
                GreenHouse.Plant.EMPTY,
                GreenHouse.Plant.READY
        });
        greenhouse.automatic();
        assertArrayEquals(new GreenHouse.Plant[] {
                GreenHouse.Plant.EMPTY,
                GreenHouse.Plant.READY,
                GreenHouse.Plant.GROWING,
                GreenHouse.Plant.EMPTY
        }, greenhouse.getPlantInfo());
    }

}
