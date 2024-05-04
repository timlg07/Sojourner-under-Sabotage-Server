package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.UserEventTrackingEntity;
import de.tim_greller.susserver.persistence.keys.UserKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserEventTrackingRepository  extends
        JpaRepository<UserEventTrackingEntity, UserKey>,
        JpaSpecificationExecutor<UserEventTrackingEntity> {
}
