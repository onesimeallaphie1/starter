package com.axia.starter.mapper;

import org.mapstruct.*;

public interface BaseMapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
