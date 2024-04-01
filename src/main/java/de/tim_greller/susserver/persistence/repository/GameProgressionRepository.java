package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.GameProgressionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface GameProgressionRepository extends
        JpaRepository<GameProgressionEntity, Integer>,
        JpaSpecificationExecutor<GameProgressionEntity> {
    @Query("SELECT MAX(gp.orderIndex) FROM GameProgressionEntity gp WHERE gp.component.name = :componentName")
    int getMaxIndex();
}
