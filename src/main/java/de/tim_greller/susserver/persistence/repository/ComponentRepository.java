package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, String>, JpaSpecificationExecutor<ComponentEntity> {
    @Transactional
    default ComponentEntity getOrCreate(String name) {
        return findById(name).orElseGet(() -> save(new ComponentEntity(name)));
    }
}
