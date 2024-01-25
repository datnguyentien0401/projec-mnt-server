package com.example.projecmntserver.dto.mapper;

public interface BaseMapper<E, D, R> {
    D toDto(E entity);
//    E toEntity(D dto);
    R toResponse(E entity);

}
