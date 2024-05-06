package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.RoomEntity;
import de.tim_greller.susserver.persistence.keys.UserRoomKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository
        extends JpaRepository<RoomEntity, UserRoomKey>, JpaSpecificationExecutor<RoomEntity> {
    @Query("""
            SELECT r
            FROM RoomEntity r
            WHERE r.userRoomKey.roomId = ?1
            AND r.userRoomKey.user.username = ?2
            """)
    Optional<RoomEntity> findByKey(int roomId, String userName);
}
