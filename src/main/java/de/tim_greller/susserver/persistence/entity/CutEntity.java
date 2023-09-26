package de.tim_greller.susserver.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class CutEntity {

    @Id
    @Column
    private String componentName;

    @Column
    private String className;

    @Column(length = 100_000)
    private String sourceCode;

}
