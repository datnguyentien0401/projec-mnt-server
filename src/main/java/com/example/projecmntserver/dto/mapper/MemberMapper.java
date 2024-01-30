package com.example.projecmntserver.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

import com.example.projecmntserver.domain.Member;
import com.example.projecmntserver.dto.request.MemberDto;
import com.example.projecmntserver.dto.response.MemberResponse;

@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper extends BaseMapper<Member, MemberDto, MemberResponse> {
}
