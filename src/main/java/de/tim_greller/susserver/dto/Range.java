package de.tim_greller.susserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Range {
    private int startLine;
    private int startColumn;
    private int endLine;
    private int endColumn;
}
