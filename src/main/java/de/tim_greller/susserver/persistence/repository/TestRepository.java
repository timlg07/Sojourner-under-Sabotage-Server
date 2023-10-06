package de.tim_greller.susserver.persistence.repository;

import de.tim_greller.susserver.persistence.entity.TestEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, UserComponentKey>, JpaSpecificationExecutor<TestEntity> {}
