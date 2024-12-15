package org.adyl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.adyl.model.Costumer;

@Repository
public interface CostumerRepository extends JpaRepository<Costumer, Integer> {
}
