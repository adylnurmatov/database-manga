package org.adyl.mapper.simplicity;

import org.adyl.model.dto.DefaultDTO;

public interface DefaultMapper<D extends DefaultDTO, E> {
    E toEntity(D dto);

    D toDTO(E entity);
}
