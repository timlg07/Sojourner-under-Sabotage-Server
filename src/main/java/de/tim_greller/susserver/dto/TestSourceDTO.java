package de.tim_greller.susserver.dto;

import de.tim_greller.susserver.persistence.entity.TestEntity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestSourceDTO extends SourceDTO {

    // Lombok is stupid
    public TestSourceDTO(String cutComponentName, String className, String sourceCode) {
        super(cutComponentName, className, sourceCode);
    }

    public static TestSourceDTO fromTestEntity(TestEntity testEntity) {
        var t = new TestSourceDTO();
        t.setCutComponentName(testEntity.getComponentName());
        t.setClassName(testEntity.getClassName());
        t.setSourceCode(testEntity.getSourceCode());
        return t;
    }
}
