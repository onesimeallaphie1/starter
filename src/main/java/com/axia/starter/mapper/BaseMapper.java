package com.axia.starter.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
