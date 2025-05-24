package org.adyl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
//    @NotNull(message = "Specify customer!")
    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;
    @NotNull(message = "Specify order date")
    @Column(name = "order_date")
    private Date orderDate;
    @Min(value = 0, message = "Minimal value is 0")
    @Column(name = "order_value")
    private Double orderValue;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> itemList;

    public Order() {
        itemList = new ArrayList<>();
    }

    public Order(Customer customer, Date orderDate, Double orderValue) {
        this.customer = customer;
        this.orderDate = orderDate;
        this.orderValue = orderValue;
        itemList = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public List<OrderItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<OrderItem> itemList) {
        this.itemList = itemList;
    }

    public void recalculateOrderValue(){
        setOrderValue(getItemList().stream().map(oi -> oi.getItemAgreedPrice()*oi.getAmount()).reduce((p1, p2) -> p1+p2).orElse(0.0));
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + customer +
                ", orderDate=" + orderDate +
                ", orderValue=" + orderValue +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            Order o2 = (Order) obj;
            return this.id.equals(o2.getId());
        }
        return false;
    }
}
