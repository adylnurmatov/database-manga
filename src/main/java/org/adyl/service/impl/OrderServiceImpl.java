package org.adyl.service.impl;

import org.springframework.stereotype.Service;
import org.adyl.mapper.abstraction.AbstractMapper;
import org.adyl.model.Customer;
import org.adyl.model.Order;
import org.adyl.model.OrderItem;
import org.adyl.model.dto.CustomerDTO;
import org.adyl.model.dto.FullOrderDTO;
import org.adyl.model.dto.MyFullOrderDTO;
import org.adyl.repository.OrderRepository;
import org.adyl.service.DefaultService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements DefaultService<FullOrderDTO, Order, Integer> {
    private final OrderRepository orderRepository;
    private final AbstractMapper mapper;

    public OrderServiceImpl(OrderRepository orderRepository, AbstractMapper mapper) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Override
    public List<FullOrderDTO> findAll() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> mapper.toDTO(order, FullOrderDTO.class)).collect(Collectors.toList());
    }

    @Override
    public FullOrderDTO findByKey(Integer key) {
        return mapper.toDTO(orderRepository.findById(key).orElse(null), FullOrderDTO.class);
    }

    public List<FullOrderDTO> findAllByCustomer(Customer customer) {
        return orderRepository.findAllByCustomer(customer).stream().map(order -> mapper.toDTO(order, FullOrderDTO.class)).collect(Collectors.toList());
    }

    @Override
    public FullOrderDTO save(FullOrderDTO obj) {
        Order order = mapper.toEntity(obj, Order.class);
        for (OrderItem orderItem : order.getItemList()) {
            orderItem.setOrder(order);
        }
        order = orderRepository.save(order);
        return mapper.toDTO(order, FullOrderDTO.class);
    }

    public FullOrderDTO saveForCustomer(FullOrderDTO orderDTO, Customer customer) {
        customer.setOrders(Collections.EMPTY_LIST);
        CustomerDTO customerDTO = mapper.toDTO(customer, CustomerDTO.class);
        orderDTO.setCustomer(customerDTO);
        return save(orderDTO);
    }

    public FullOrderDTO saveForCustomer(MyFullOrderDTO myOrderDTO, Customer customer) {
        return saveForCustomer(mapper.toDTO(myOrderDTO, FullOrderDTO.class), customer);
    }

    @Override
    public FullOrderDTO update(Integer key, FullOrderDTO obj) {
        Order order = mapper.toEntity(obj, Order.class);
        for (OrderItem orderItem : order.getItemList()) {
            orderItem.setOrder(order);
        }
        order.setId(key);
        orderRepository.save(order);
        return mapper.toDTO(order, FullOrderDTO.class);
    }

    @Override
    public void delete(Integer key) {
        orderRepository.deleteById(key);
    }
}
