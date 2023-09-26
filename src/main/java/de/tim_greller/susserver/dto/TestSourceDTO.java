package de.tim_greller.susserver.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestSourceDTO extends SourceDTO {

    // Lombok is stupid
    public TestSourceDTO(String cutComponentName, String className, String sourceCode) {
        super(cutComponentName, className, sourceCode);
    }
}
