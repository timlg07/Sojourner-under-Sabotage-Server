package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.UserModifiedCutEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserModifiedCutRepository extends
        JpaRepository<UserModifiedCutEntity, UserComponentKey>,
        JpaSpecificationExecutor<UserModifiedCutEntity> {

    @Query("""
            SELECT u
            FROM UserModifiedCutEntity u
            WHERE u.userComponentKey.component.name = :componentName
            AND u.userComponentKey.user.username = :userId
            """)
    Optional<UserModifiedCutEntity> findByKey(String componentName, String userId);

    @Transactional
    @Modifying
    @Query("""
            DELETE FROM UserModifiedCutEntity u
            WHERE u.userComponentKey.component.name = :componentName
            AND u.userComponentKey.user.username = :userId
            """)
    void deleteByKey(String componentName, String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserModifiedCutEntity u WHERE u.userComponentKey.user.username = :username")
    void deleteByUserId(String username);
}
