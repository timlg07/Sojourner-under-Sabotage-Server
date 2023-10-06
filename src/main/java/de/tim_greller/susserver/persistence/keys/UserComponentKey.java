package de.tim_greller.susserver.persistence.keys;

import java.io.Serializable;

import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Embeddable
@Data
public class UserComponentKey implements Serializable {

    @ManyToOne
    @JoinColumn
    private ComponentEntity component;

    @ManyToOne
    @JoinColumn
    private UserEntity user;
}
