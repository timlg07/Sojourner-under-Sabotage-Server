package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.TestEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository
        extends JpaRepository<TestEntity, UserComponentKey>, JpaSpecificationExecutor<TestEntity> {

    //
    @Query("""
            SELECT t
            FROM TestEntity t
            WHERE t.userComponentKey.component.name = ?1
            AND t.userComponentKey.user.email = ?2
            """)
    Optional<TestEntity> findByKey(String componentName, String userEmail);
}
