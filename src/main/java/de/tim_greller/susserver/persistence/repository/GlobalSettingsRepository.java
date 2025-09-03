package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.GlobalSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalSettingsRepository extends
        JpaRepository<GlobalSettingsEntity, String>,
        JpaSpecificationExecutor<GlobalSettingsEntity> {
}
