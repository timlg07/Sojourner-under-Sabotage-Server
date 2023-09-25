package de.tim_greller.susserver.dto;

import de.tim_greller.susserver.model.game.ComponentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestSourceDTO {
    private String className;
    private String sourceCode;
}
