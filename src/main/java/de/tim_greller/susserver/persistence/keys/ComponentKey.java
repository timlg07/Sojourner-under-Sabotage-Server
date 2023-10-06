package de.tim_greller.susserver.persistence.keys;

import java.io.Serializable;

import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Embeddable
@Data
public class ComponentKey implements Serializable {

    @OneToOne
    @JoinColumn
    private ComponentEntity component;

}
