package de.tim_greller.susserver.persistence.entity;

import de.tim_greller.susserver.persistence.keys.ComponentStageKey;
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
public class PatchEntity {

    @Column
    private String patch;

    @EmbeddedId
    private ComponentStageKey componentKey;

}
