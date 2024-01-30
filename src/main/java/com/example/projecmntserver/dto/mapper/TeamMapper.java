package com.example.projecmntserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.request.TeamDto;
import com.example.projecmntserver.dto.response.TeamResponse;

@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper extends BaseMapper<Team, TeamDto, TeamResponse> {
}
