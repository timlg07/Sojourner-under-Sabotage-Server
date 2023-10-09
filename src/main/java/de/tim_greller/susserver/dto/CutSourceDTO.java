package de.tim_greller.susserver.dto;

import java.util.List;

import de.tim_greller.susserver.persistence.entity.CutEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CutSourceDTO extends SourceDTO {

    // Lombok is stupid
    public CutSourceDTO(String cutComponentName, String className, String sourceCode, List<Range> editable) {
        super(cutComponentName, className, sourceCode, editable);
    }

    public static CutSourceDTO fromCutEntity(CutEntity cutEntity) {
        var c = new CutSourceDTO();
        c.setCutComponentName(cutEntity.getComponentKey().getComponent().getName());
        c.setClassName(cutEntity.getClassName());
        c.setSourceCode(cutEntity.getSourceCode());
        return c;
    }
}
