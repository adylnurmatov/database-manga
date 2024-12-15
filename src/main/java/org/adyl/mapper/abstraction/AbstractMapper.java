package org.adyl.mapper.abstraction;

import org.adyl.model.dto.DefaultDTO;

public interface AbstractMapper {
    <D extends DefaultDTO, E> E toEntity(D dto, Class<E> entity);

    <D extends DefaultDTO, E> D toDTO(E entity, Class<D> dto);
}
