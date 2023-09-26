package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.CutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CutRepository extends JpaRepository<CutEntity, String>, JpaSpecificationExecutor<CutEntity> {
}
