package de.tim_greller.susserver.persistence.entity;

import com.github.difflib.patch.Patch;
import de.tim_greller.susserver.persistence.keys.ComponentForeignKey;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PatchEntity {

    @Id
    @GeneratedValue
    private int id;

    @Column
    private Patch<String> patch;

    @Embedded
    private ComponentForeignKey componentKey;

    public PatchEntity(Patch<String> patch, ComponentForeignKey componentKey) {
        this.patch = patch;
        this.componentKey = componentKey;
    }
}
