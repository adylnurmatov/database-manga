package org.adyl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.adyl.model.RefContactType;

@Repository
public interface RefContactTypeRepository extends JpaRepository<RefContactType, Integer> {
}
