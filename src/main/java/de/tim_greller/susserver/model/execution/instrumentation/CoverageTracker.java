package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static de.tim_greller.susserver.util.Utils.mapMap;

import lombok.Getter;

/**
 * Tracks coverage information, i.e., which line was visited how many times.
 */
// Needs to be public to be callable during test execution.
public class CoverageTracker {

    private static final CoverageTracker INSTANCE = new CoverageTracker();
    private static final Map<String, ClassTracker> classTrackers = new TreeMap<>();

    private CoverageTracker() {
    }

    public static CoverageTracker getInstance() {
        return INSTANCE;
    }

    public Map<String, ClassTracker> getClassTrackers() {
        return classTrackers;
    }

    /**
     * Track a visit of a line.
     *
     * @param pLineNumber The line number that was tracked
     * @param pClassName  The class in which the line is
     */
    // Needs to be public to be callable during test execution.
    public static void trackLineVisit(final int pLineNumber, final String pClassName) {
        if (classTrackers.containsKey(pClassName)) {
            classTrackers.get(pClassName).visitLine(pLineNumber);
        } else {
            final ClassTracker classTracker = new ClassTracker();
            classTracker.visitLine(pLineNumber);
            classTrackers.put(pClassName, classTracker);
        }
    }

    /**
     * Track the existence of a line.
     *
     * @param pLineNumber The line number to track
     * @param pClassName  The class in which the line is
     */
    // Needs to be public to be callable during test execution.
    public static void trackLine(final int pLineNumber, final String pClassName) {
        if (classTrackers.containsKey(pClassName)) {
            classTrackers.get(pClassName).trackLine(pLineNumber);
        } else {
            final ClassTracker classTracker = new ClassTracker();
            classTracker.trackLine(pLineNumber);
            classTrackers.put(pClassName, classTracker);
        }
    }

    public Map<String, Map<Integer, Integer>> getCoverage() {
        return mapMap(classTrackers, (className, classTracker) -> classTracker.getVisitedLines());
    }

    public Map<String, Set<Integer>> getLines() {
        return mapMap(classTrackers, (className, classTracker) -> classTracker.getLines());
    }

    @Getter
    public static class ClassTracker {
        private final Map<Integer, Integer> visitedLines = new TreeMap<>();
        private final Set<Integer> lines = new HashSet<>();

        void visitLine(final int pLineNumber) {
            if (visitedLines.containsKey(pLineNumber)) {
                visitedLines.merge(pLineNumber, 1, Integer::sum);
            } else {
                visitedLines.put(pLineNumber, 1);
            }
        }

        void trackLine(final int pLineNumber) {
            lines.add(pLineNumber);
        }

    }
}
