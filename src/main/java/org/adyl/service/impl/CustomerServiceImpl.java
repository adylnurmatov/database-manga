package org.adyl.service.impl;

import org.hibernate.Hibernate;
import org.adyl.security.models.StoreUser;
import org.adyl.security.repositories.StoreUserRepository;
import org.springframework.stereotype.Service;
import org.adyl.mapper.abstraction.AbstractMapper;
import org.adyl.model.Customer;
import org.adyl.model.dto.CustomerDTO;
import org.adyl.repository.CustomerRepository;
import org.adyl.service.DefaultService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements DefaultService<CustomerDTO, Customer, Integer> {
    private final CustomerRepository customerRepository;
    private final StoreUserRepository userRepository;
    private final AbstractMapper mapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, StoreUserRepository userRepository, AbstractMapper mapper) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public List<CustomerDTO> findAll() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(customer -> {
            Hibernate.initialize(customer.getOrders());
            return mapper.toDTO(customer, CustomerDTO.class);
        }).collect(Collectors.toList());
    }

    @Override
    public CustomerDTO findByKey(Integer key) {
        Customer customer = customerRepository.findById(key).orElse(null);
        Hibernate.initialize(customer.getOrders());
        return mapper.toDTO(customer, CustomerDTO.class);
    }

    @Override
    public CustomerDTO save(CustomerDTO obj) {
        Customer customer = customerRepository.save(mapper.toEntity(obj, Customer.class));
        return mapper.toDTO(customer, CustomerDTO.class);
    }

    @Override
    public CustomerDTO update(Integer key, CustomerDTO obj) {
        Customer customer = mapper.toEntity(obj, Customer.class);
        customer.setId(key);
        customerRepository.save(customer);
        return mapper.toDTO(customer, CustomerDTO.class);
    }

    @Transactional
    public void deleteUserForCustomer(Customer customer) {
        StoreUser user = userRepository.findByCustomer(customer);
        user.setUsername("AAA");
    }

    @Override
    public void delete(Integer key) {
        customerRepository.deleteById(key);
    }
}
