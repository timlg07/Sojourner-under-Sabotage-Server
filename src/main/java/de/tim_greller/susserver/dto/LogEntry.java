package de.tim_greller.susserver.dto;

public record LogEntry(String message, String methodName, int lineNumber) {
}
