package com.example.projecmntserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

import com.example.projecmntserver.domain.Planning;
import com.example.projecmntserver.dto.response.PlanningResponse;

@FunctionalInterface
@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanningMapper {
    PlanningResponse toResponse(Planning planning);
}
