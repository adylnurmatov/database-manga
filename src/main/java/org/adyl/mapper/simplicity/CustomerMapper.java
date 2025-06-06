package org.adyl.mapper.simplicity;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.adyl.model.Customer;
import org.adyl.model.dto.CustomerDTO;

@Component
public class CustomerMapper implements DefaultMapper<CustomerDTO, Customer> {
    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Customer toEntity(CustomerDTO dto) {
        return (dto == null) ? null : modelMapper.map(dto, Customer.class);
    }

    @Override
    public CustomerDTO toDTO(Customer entity) {
        return (entity == null) ? null : modelMapper.map(entity, CustomerDTO.class);
    }
}
