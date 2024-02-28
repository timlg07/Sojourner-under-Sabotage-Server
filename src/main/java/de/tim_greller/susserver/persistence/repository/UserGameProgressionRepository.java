package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.UserGameProgressionEntity;
import de.tim_greller.susserver.persistence.keys.UserKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameProgressionRepository extends
        JpaRepository<UserGameProgressionEntity, UserKey>,
        JpaSpecificationExecutor<UserGameProgressionEntity> {
}
