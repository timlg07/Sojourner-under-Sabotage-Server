package de.tim_greller.susserver.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameProgressionEntity {
    @Id
    private int orderIndex;

    private int roomId;

    @ManyToOne
    private ComponentEntity component;

    private int stage;

    private int delaySeconds;
}
