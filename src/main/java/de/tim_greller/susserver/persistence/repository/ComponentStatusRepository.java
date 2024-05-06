package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.ComponentStatusEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentStatusRepository extends
        JpaRepository<ComponentStatusEntity, UserComponentKey>,
        JpaSpecificationExecutor<ComponentStatusEntity> {

    @Query("""
            SELECT c
            FROM ComponentStatusEntity c
            WHERE c.userComponentKey.user.username = :userId
            AND c.userComponentKey.component.name = :componentName
            """)
    Optional<ComponentStatusEntity> findByKey(String componentName, String userId);

    void deleteAllByUserComponentKeyUserUsername(String userId);
}
