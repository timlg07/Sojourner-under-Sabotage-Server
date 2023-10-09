package de.tim_greller.susserver.dto;

import java.util.List;

import de.tim_greller.susserver.persistence.entity.TestEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestSourceDTO extends SourceDTO {

    // Lombok is stupid
    public TestSourceDTO(String cutComponentName, String className, String sourceCode, List<Range> editable) {
        super(cutComponentName, className, sourceCode, editable);
    }

    public static TestSourceDTO fromTestEntity(TestEntity testEntity) {
        var t = new TestSourceDTO();
        t.setCutComponentName(testEntity.getUserComponentKey().getComponent().getName());
        t.setClassName(testEntity.getClassName());
        t.setSourceCode(testEntity.getSourceCode());
        return t;
    }
}
