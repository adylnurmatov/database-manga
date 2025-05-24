package org.adyl.security.repositories;

import org.adyl.model.Customer;
import org.adyl.security.models.StoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StoreUserRepository extends JpaRepository<StoreUser, Long> {
//    Optional<StoreUser> findByUsername(String username);

    @Query("select u from StoreUser u JOIN fetch u.customer where u.username = :username")//JPQL with join fetch. One important think to mention here, is that you select here Whole objects, so you must use aliases!!! Also JOIN FETCH is used to load Lazy props in eager mode
    Optional<StoreUser> findByUsername(String username);

    StoreUser findByCustomer(Customer customer);

}
