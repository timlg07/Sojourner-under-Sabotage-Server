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

    public static void trackVar(final Object value, final int pVarIndex, final String pClassName, final String methodName) {
        if (classTrackers.containsKey(pClassName)) {
            classTrackers.get(pClassName).trackVariableValueChanged(value, pVarIndex, methodName);
        } else {
            final ClassTracker classTracker = new ClassTracker();
            classTracker.trackVariableValueChanged(value, pVarIndex, methodName);
            classTrackers.put(pClassName, classTracker);
        }
        System.out.println(pVarIndex + " " + pClassName);
    }
    public static void trackVar(final int value, final int pVarIndex, final String pClassName, final String methodName) {
        trackVar((Integer) value, pVarIndex, pClassName, methodName);
    }
    public static void trackVar(final long value, final int pVarIndex, final String pClassName, final String methodName) {
        trackVar((Long) value, pVarIndex, pClassName, methodName);
    }
    public static void trackVar(final float value, final int pVarIndex, final String pClassName, final String methodName) {
        trackVar((Float) value, pVarIndex, pClassName, methodName);
    }
    public static void trackVar(final double value, final int pVarIndex, final String pClassName, final String methodName) {
        trackVar((Double) value, pVarIndex, pClassName, methodName);
    }

    public static void trackVarDef(final int pVarIndex, final String pVarName, final String pVarDesc,
                                   final String pClassName, final String methodName) {
        if (classTrackers.containsKey(pClassName)) {
            classTrackers.get(pClassName).trackVariableDefinition(pVarIndex, methodName + "/" + pVarName, pVarDesc, methodName);
        } else {
            final ClassTracker classTracker = new ClassTracker();
            classTracker.trackVariableDefinition(pVarIndex, methodName + "/" + pVarName, pVarDesc, methodName);
            classTrackers.put(pClassName, classTracker);
        }
        System.out.println(pVarIndex + " " + pVarName + " " + pVarDesc + " " + pClassName+"::"+methodName);
    }

    public static String getVarType(final int pVarIndex, final String pClassName, final String methodName) {
        if (classTrackers.containsKey(pClassName)) {
            return classTrackers.get(pClassName).getVarType(pVarIndex, methodName);
        } else {
            return null;
        }
    }

    public static void log(final String msg) {//final PrintStream _ignored,
        System.out.println(msg);
    }

    public Map<String, Map<Integer, Integer>> getCoverage() {
        return mapMap(classTrackers, (className, classTracker) -> classTracker.getVisitedLines());
    }

    public Map<String, Set<Integer>> getLines() {
        return mapMap(classTrackers, (className, classTracker) -> classTracker.getLines());
    }

    public Map<String, Map<Integer, Map<String, Object>>> getVars() {
        return mapMap(classTrackers, (className, classTracker) -> classTracker.getVars());
    }

    @Getter
    public static class ClassTracker {
        private int lastVisitedLine = 0;
        private final Map<Integer, Integer> visitedLines = new TreeMap<>();
        private final Set<Integer> lines = new HashSet<>();
        private final Map<String, String[]> currentIndexToVarNameAndDescriptor = new TreeMap<>();
        private final Map<Integer, Map<String, Object>> vars = new TreeMap<>();

        void visitLine(final int pLineNumber) {
            if (visitedLines.containsKey(pLineNumber)) {
                visitedLines.merge(pLineNumber, 1, Integer::sum);
            } else {
                visitedLines.put(pLineNumber, 1);
            }
            lastVisitedLine = pLineNumber;
        }

        void trackLine(final int pLineNumber) {
            lines.add(pLineNumber);
        }

        void trackVariableValueChanged(final Object value, final int pVarIndex, final String methodName) {
            String varId = methodName + "/" + pVarIndex;
            if (!currentIndexToVarNameAndDescriptor.containsKey(varId)) {
                System.out.println("No var def found for " + varId);
                return;
            }
            String varName = currentIndexToVarNameAndDescriptor.get(varId)[0];
            if (vars.containsKey(lastVisitedLine)) {
                vars.get(lastVisitedLine).put(varName, value);
            } else {
                final Map<String, Object> varMap = new TreeMap<>();
                varMap.put(varName, value);
                vars.put(lastVisitedLine, varMap);
            }
        }

        public void trackVariableDefinition(final int pVarIndex, final String pVarName, String pVarDesc, final String methodName) {
            String varId = methodName + "/" + pVarIndex;
            currentIndexToVarNameAndDescriptor.put(varId, new String[] {pVarName, pVarDesc});
        }

        public String getVarType(final int pVarIndex, String methodName) {
            String varId = methodName + "/" + pVarIndex;
            if (currentIndexToVarNameAndDescriptor.containsKey(varId)) {
                return currentIndexToVarNameAndDescriptor.get(varId)[1];
            } else {
                return null;
            }
        }

        public void clear() {
            visitedLines.clear();
            lines.clear();
            currentIndexToVarNameAndDescriptor.clear();
            vars.clear();
        }
    }
}
