package de.tim_greller.susserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class SourceDTO {
    protected String cutComponentName;
    protected String className;
    protected String sourceCode;
}
