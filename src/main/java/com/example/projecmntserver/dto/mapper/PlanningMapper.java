//package com.example.projecmntserver.dto.mapper;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.MapperConfig;
//import org.mapstruct.Mapping;
//import org.mapstruct.Mappings;
//import org.mapstruct.Named;
//import org.mapstruct.ReportingPolicy;
//import org.springframework.util.StringUtils;
//
//import com.example.projecmntserver.domain.Planning;
//import com.example.projecmntserver.dto.response.PlanningResponseDto;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Mapper(componentModel = "spring")
//@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
//public interface PlanningMapper {
//    @Mappings({
//            @Mapping(target = "availableWorkingColumns",
//                    qualifiedByName = "convertStringToObject"),
//            @Mapping(target = "availableWorkingData",
//                    qualifiedByName = "convertStringToObject"),
//            @Mapping(target = "requiredWorkforceData",
//                    qualifiedByName = "convertStringToObject"),
//            @Mapping(target = "totalWorkforceData",
//                    qualifiedByName = "convertStringToObject"),
//            @Mapping(target = "annualLeaveData",
//                    qualifiedByName = "convertStringToObject")
//    })
//    PlanningResponseDto toResponse(Planning entity);
//    List<PlanningResponseDto> toResponse(List<Planning> entity);
//
//    @Named("convertStringToObject")
//    default Map convertStringToObject(String raw) throws JsonProcessingException {
//        if (!StringUtils.hasText(raw)) {
//            return new HashMap<>();
//        }
//        final ObjectMapper mapper = new ObjectMapper();
//        return mapper.readValue(raw, Map.class);
//    }
//}
