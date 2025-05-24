package org.adyl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.adyl.model.Customer;
import org.adyl.model.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("select distinct o from Order o left join fetch o.itemList where o.customer = :customer")
    List<Order> findAllByCustomer(Customer customer);

    @Query("select distinct o from Order o left join fetch o.itemList")
    List<Order> findFullAllOrders();

    @Query("select o from Order o join fetch o.itemList where o.id = :id")
    Order findFullById(Integer id);
}
