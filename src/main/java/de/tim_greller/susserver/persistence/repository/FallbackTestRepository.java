package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.FallbackTestEntity;
import de.tim_greller.susserver.persistence.keys.ComponentStageKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FallbackTestRepository extends
        JpaRepository<FallbackTestEntity, ComponentStageKey>,
        JpaSpecificationExecutor<FallbackTestEntity> {

    @Query("""
            SELECT f
            FROM FallbackTestEntity f
            WHERE f.componentStageKey.component.name = :componentName
            AND f.componentStageKey.stage = :stage
            """)
    Optional<FallbackTestEntity> findByKey(String componentName, int stage);
}
