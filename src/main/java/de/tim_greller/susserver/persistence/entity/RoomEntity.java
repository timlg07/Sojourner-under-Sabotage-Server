package de.tim_greller.susserver.persistence.entity;

import de.tim_greller.susserver.persistence.keys.UserRoomKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomEntity {

    @EmbeddedId
    private UserRoomKey userRoomKey;

    private boolean unlocked;

}
