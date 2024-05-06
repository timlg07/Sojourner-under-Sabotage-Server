package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.ActivePatchEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivePatchRepository extends JpaRepository<ActivePatchEntity, UserComponentKey>,
        JpaSpecificationExecutor<ActivePatchEntity> {

    @Query("""
            SELECT a
            FROM ActivePatchEntity a
            WHERE a.componentKey.component.name = ?1
            AND a.componentKey.user.username = ?2
            """)
    Optional<ActivePatchEntity> findByKey(String componentName, String userId);

    void deleteAllByComponentKeyUserUsername(String userId);
}
