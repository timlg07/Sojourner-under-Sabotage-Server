package de.tim_greller.susserver.persistence.entity;

import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentStatusEntity {

    @EmbeddedId
    private UserComponentKey userComponentKey;

    @Setter
    private int stage = 1;

    @Setter
    private boolean testsActivated = false;

}
