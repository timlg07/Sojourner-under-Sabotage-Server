package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.GameProgressionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameProgressionRepository extends
        JpaRepository<GameProgressionEntity, Integer>,
        JpaSpecificationExecutor<GameProgressionEntity> {
}
