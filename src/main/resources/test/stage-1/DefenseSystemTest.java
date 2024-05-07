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

public class DefenseSystemTest {

    @Test
    public void testMove() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(1, 1);
        assertEquals(1, spaceShip.getCurrentPosX(), 0.01);
        assertEquals(1, spaceShip.getCurrentPosY(), 0.01);
    }

    @Test
    public void testMoveTwice() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(1, 1.5);
        spaceShip.move(2, 2.5);
        assertEquals(3, spaceShip.getCurrentPosX(), 0.01);
        assertEquals(4, spaceShip.getCurrentPosY(), 0.01);
    }

    @Test
    public void testDistanceTo() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(1, 1);
        assertEquals(Math.sqrt(2), spaceShip.distanceTo(2, 2), 0.01);
    }

    @Test
    public void testDistanceToZero() {
        DefenseSystem spaceShip = new DefenseSystem();
        assertEquals(0, spaceShip.distanceTo(0, 0), 0.01);
    }
    
    @Test
    public void testIsWithinRange() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(2, 2);
        assertTrue(spaceShip.isWithinRange(-1, 1));
    }

    @Test
    public void testNotInRange() {
        DefenseSystem spaceShip = new DefenseSystem();
        assertFalse(spaceShip.isWithinRange(10.1, 0));
    }

    @Test
    public void testFleeFrom() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(2, 2);
        spaceShip.fleeFrom(-1, 1);
        assertEquals(8.48, spaceShip.getCurrentPosX(), 0.01);
        assertEquals(4.16, spaceShip.getCurrentPosY(), 0.01);
    }

    @Test
    public void testFleeFrom2() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(0, 5);
        spaceShip.fleeFrom(5, 0);
        assertEquals(-2.07, spaceShip.getCurrentPosX(), 0.01);
        assertEquals( 7.07, spaceShip.getCurrentPosY(), 0.01);
    }

    @Test
    public void testNotInRangeAfterFlee() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(3, 4);
        spaceShip.fleeFrom(5, 1);
        assertFalse(spaceShip.isWithinRange(5, 0));
    }

    @Test
    public void testFleeFromNotInRange() {
        DefenseSystem spaceShip = new DefenseSystem();
        spaceShip.move(-2, 0);
        spaceShip.fleeFrom(10, 10);
        assertEquals(-2, spaceShip.getCurrentPosX(), 0.01);
        assertEquals(0, spaceShip.getCurrentPosY(), 0.01);
    }
}
