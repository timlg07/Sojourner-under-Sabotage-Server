package de.tim_greller.susserver.model.execution.instrumentation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class OutputWriter {

  private OutputWriter() {}

  public static void writeShellOutput(final Map<String, InstrumentationTracker.ClassTracker> pTrackedMap) {
    for (Entry<String, InstrumentationTracker.ClassTracker> tracker : pTrackedMap.entrySet()) {
      final String className = tracker.getKey();
      final InstrumentationTracker.ClassTracker classTracker = tracker.getValue();
      final Set<Integer> lines = classTracker.getLines();
      final Map<Integer, Integer> visitedLines = classTracker.getVisitedLines();
      final int linesSize = lines.size();
      final int visitedLinesSize = visitedLines.size();

      System.out.printf("Coverage Data for Class: %s\n", className);

      for (Entry<Integer, Integer> entry : visitedLines.entrySet()) {
        System.out.printf("    line %d visited %d times\n", entry.getKey(), entry.getValue());
      }

      System.out.printf("    number of lines %d\n", linesSize);
      System.out.printf(
          "    line coverage %2.2f%% (%d/%d)\n",
          (float) visitedLinesSize / linesSize * 100, visitedLinesSize, linesSize);

      System.out.println();
      System.out.println();
    }
  }
}
