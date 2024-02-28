package de.tim_greller.susserver.persistence.keys;

import java.io.Serializable;

import de.tim_greller.susserver.persistence.entity.UserEntity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserKey implements Serializable {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private UserEntity user;

}
