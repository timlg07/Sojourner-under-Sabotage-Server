package de.tim_greller.susserver.persistence.entity;

import de.tim_greller.susserver.persistence.keys.UserKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGameProgressionEntity {
    @EmbeddedId
    private UserKey user;

    @ManyToOne
    private GameProgressionEntity gameProgression;
}
