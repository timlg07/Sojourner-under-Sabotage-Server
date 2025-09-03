package de.tim_greller.susserver.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class GlobalSettingsEntity {

    /*
     * Cannot use key/value as name, because these are reserved and therefore the whole entity
     * will be skipped during DDL initialization.
     */

    @Id
    @Column
    private String settingsKey;

    @Column
    private String settingsValue;

}
