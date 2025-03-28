package de.tim_greller.susserver.persistence.repository;

import java.util.Optional;

import de.tim_greller.susserver.persistence.entity.PatchEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PatchRepository extends
        JpaRepository<PatchEntity, ComponentKey>,
        JpaSpecificationExecutor<PatchEntity> {
    Optional<PatchEntity> findPatchEntitiesByComponentKey_ComponentNameAndComponentKey_Stage(String componentName, int stage);
}
