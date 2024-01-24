package com.example.projecmntserver.dto.mapper;

import java.util.List;

public interface BaseMapper<E, D, R> {
    D toDto(E entity);
    E toEntity(D dto);
    R toResponse(E entity);
    List<R> toResponse(List<E> entity);

}
