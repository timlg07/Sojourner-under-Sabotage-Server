package de.tim_greller.susserver.persistence.entity;

import de.tim_greller.susserver.persistence.keys.ComponentKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CutEntity {

    @EmbeddedId
    private ComponentKey componentKey;

    @Column
    private String className;

    @Column(length = 100_000)
    private String sourceCode;

}
