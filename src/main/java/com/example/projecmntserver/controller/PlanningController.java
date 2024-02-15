package com.example.projecmntserver.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projecmntserver.domain.Planning;
import com.example.projecmntserver.dto.request.PlanningDto;
import com.example.projecmntserver.dto.response.PlanningResponse;
import com.example.projecmntserver.service.PlanningService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/plannings")
@Validated
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;

    @PostMapping
    public ResponseEntity<Planning> create(@RequestBody PlanningDto planningDto)
            throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                planningService.create(planningDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Planning> update(@PathVariable("id") Long id,
                                           @RequestBody PlanningDto planningDto)
            throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.OK).body(
                planningService.update(id, planningDto));
    }

    @GetMapping
    public ResponseEntity<List<PlanningResponse>> getAll() throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.OK).body(
                planningService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> delete(@PathVariable("id") Long id) throws JsonProcessingException {
        planningService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
