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

public class RnaAnalyzerTest {

    @Test
    public void hiddenTest1() {
        RnaAnalyzer analyzer = new RnaAnalyzer("AAGC");
        assertEquals("AG", analyzer.longestCommonRnaSubsequence("AGUA"));
    }

    @Test
    public void hiddenTest2() {
        RnaAnalyzer analyzer = new RnaAnalyzer("AAGAUGCCGU");
        assertEquals("AAGAUGCCGU", analyzer.longestCommonRnaSubsequence("AAGCCGAUGCCGUGCC"));
    }

    @Test
    public void testStripCharacters1() {
        RnaAnalyzer analyzer = new RnaAnalyzer("AA");
        assertEquals("AA", analyzer.longestCommonRnaSubsequence("AHello World!A"));
    }

    @Test
    public void testStripCharacters2() {
        RnaAnalyzer analyzer = new RnaAnalyzer("A Test A");
        assertEquals("AA", analyzer.longestCommonRnaSubsequence("AA"));
    }

    @Test
    public void testNoCommonSubsequence1() {
        RnaAnalyzer analyzer = new RnaAnalyzer("AAGAUGCCGU");
        assertEquals("", analyzer.longestCommonRnaSubsequence(""));
    }

    @Test
    public void testNoCommonSubsequence2() {
        RnaAnalyzer analyzer = new RnaAnalyzer("AAG");
        assertEquals("", analyzer.longestCommonRnaSubsequence("CUC"));
    }

    @Test
    public void hiddenTest3() {
        RnaAnalyzer analyzer = new RnaAnalyzer("AAGAUGCCGU");
        assertEquals("GAGCGU", analyzer.longestCommonRnaSubsequence("CGAGAAACGUACC"));
    }

}
