package org.adyl.mapper.abstraction;

import jakarta.annotation.PostConstruct;
import org.adyl.security.models.StoreUser;
import org.adyl.security.models.dto.StoreRegistrationUserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.adyl.model.dto.DefaultDTO;

@Component
public class AbstractMapperImpl implements AbstractMapper {
    @Autowired
    private ModelMapper modelMapper;

    @PostConstruct
    public void setup() {
        modelMapper.typeMap(StoreRegistrationUserDTO.class, StoreUser.class)
                .addMappings(mapper -> mapper.map(StoreRegistrationUserDTO::getEmail, StoreUser::setEmail));
    }

    @Override
    public <D extends DefaultDTO, E> E toEntity(D dto, Class<E> entity) {
        return (dto == null) ? null : modelMapper.map(dto, entity);
    }

    @Override
    public <D extends DefaultDTO, E> D toDTO(E entity, Class<D> dto) {
        return (entity == null) ? null : modelMapper.map(entity, dto);
    }
}

