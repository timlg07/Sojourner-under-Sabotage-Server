package de.tim_greller.susserver.dto;

import java.util.ArrayList;
import java.util.List;

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
    protected List<Range> editable = new ArrayList<>(1);

    public SourceDTO(CutSourceDTO cut) {
        this.cutComponentName = cut.getCutComponentName();
        this.className = cut.getClassName();
        this.sourceCode = cut.getSourceCode();
        this.editable = new ArrayList<>(cut.getEditable());
    }

    @SuppressWarnings("unchecked")
    public final <T extends SourceDTO> T restrictTo(Range range) {
        try {
            editable.add(range);
        } catch (UnsupportedOperationException e) {
            // handle ImmutableCollections
            editable = new ArrayList<>(editable);
            editable.add(range);
        }
        return (T) this;
    }
}
