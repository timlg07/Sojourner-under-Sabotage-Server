package de.tim_greller.susserver.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class UserEventTrackingEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn
    private UserEntity user;

    private long timestamp;

    private String eventType;

    @Column(length = 800_000)
    private String json;

}
