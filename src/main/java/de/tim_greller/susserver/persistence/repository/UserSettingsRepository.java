package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.UserSettingsEntity;
import de.tim_greller.susserver.persistence.keys.UserKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserSettingsRepository extends
        JpaRepository<UserSettingsEntity, UserKey>,
        JpaSpecificationExecutor<UserSettingsEntity> {
}
