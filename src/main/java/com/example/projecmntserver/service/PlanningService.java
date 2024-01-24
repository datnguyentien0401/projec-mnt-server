package com.example.projecmntserver.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.example.projecmntserver.domain.Planning;
import com.example.projecmntserver.dto.request.PlanningDto;
import com.example.projecmntserver.dto.response.PlanningResponse;
import com.example.projecmntserver.repository.PlanningRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningService {
    private final PlanningRepository planningRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public Planning create(PlanningDto planningDto) throws JsonProcessingException {
        final var planning = new Planning();
        planning.setTableKey(planningDto.getKey());
        planning.setName(planningDto.getName());
        modifyPlanning(planning, planningDto);
        return planningRepository.save(planning);
    }

    public Planning update(Long id, PlanningDto planningDto) throws NotFoundException, JsonProcessingException {
        final var planning = planningRepository.findById(id).orElseThrow(NotFoundException::new);
        modifyPlanning(planning, planningDto);
        return planningRepository.save(planning);
    }

    private static void modifyPlanning(Planning planning, PlanningDto planningDto) throws JsonProcessingException {
        final String availableWorkingData = OBJECT_MAPPER.writeValueAsString(
                planningDto.getAvailableWorkingData());
        final String requiredWorkforceData = OBJECT_MAPPER.writeValueAsString(
                planningDto.getRequiredWorkforceData());
        final String totalWorkforceData = OBJECT_MAPPER.writeValueAsString(planningDto.getTotalWorkforceData());
        final String annualLeaveData = OBJECT_MAPPER.writeValueAsString(planningDto.getAnnualLeaveData());
        planning.setAvailableWorkingData(availableWorkingData);
        planning.setRequiredWorkforceData(requiredWorkforceData);
        planning.setTotalWorkforceData(totalWorkforceData);
        planning.setAnnualLeaveData(annualLeaveData);
        planning.setFromDate(planningDto.getFromDate());
        planning.setToDate(planningDto.getToDate());
    }

    public List<PlanningResponse> getAll() throws JsonProcessingException {
        final List<PlanningResponse> result = new ArrayList<>();
        final var plannings = planningRepository.findAll();
        for (var planning : plannings) {
            final PlanningResponse responseDto = new PlanningResponse();
            responseDto.setId(planning.getId());
            responseDto.setKey(planning.getTableKey());
            responseDto.setName(planning.getName());
            responseDto.setFromDate(planning.getFromDate());
            responseDto.setToDate(planning.getToDate());
            responseDto.setCreatedAt(planning.getCreatedAt());
            responseDto.setUpdatedAt(planning.getUpdatedAt());
            responseDto.setAvailableWorkingData(
                    OBJECT_MAPPER.readValue(planning.getAvailableWorkingData(), Object.class));
            responseDto.setTotalWorkforceData(
                    OBJECT_MAPPER.readValue(planning.getTotalWorkforceData(), Object.class));
            responseDto.setRequiredWorkforceData(
                    OBJECT_MAPPER.readValue(planning.getRequiredWorkforceData(), Object.class));
            responseDto.setAnnualLeaveData(
                    OBJECT_MAPPER.readValue(planning.getAnnualLeaveData(), Object.class));
            result.add(responseDto);
        }
        return result;
    }

    public void delete(Long id) {
        planningRepository.deleteById(id);
    }
}
