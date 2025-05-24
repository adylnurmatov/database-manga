package org.adyl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.adyl.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
