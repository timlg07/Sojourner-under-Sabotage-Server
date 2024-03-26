package de.tim_greller.susserver.dto;

public record LogEntry(int orderIndex, String message, String methodName, int lineNumber, String testMethodName) {
}
