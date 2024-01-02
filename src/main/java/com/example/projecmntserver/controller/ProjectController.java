package com.example.projecmntserver.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.projecmntserver.dto.ProjectStatisticResponseDto;
import com.example.projecmntserver.dto.jira.ProjectDto;
import com.example.projecmntserver.service.ProjectService;
import com.example.projecmntserver.util.DatetimeUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@Validated
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/search")
    public ResponseEntity<ProjectStatisticResponseDto> search(
            @RequestParam(required = false) List<String> projectIds,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return ResponseEntity.ok(projectService.getProjectStatistic(projectIds, DatetimeUtils.parse(fromDate),
                                                                    DatetimeUtils.parse(toDate)));
    }

    @GetMapping
    public ResponseEntity<ProjectDto[]> getAll() {
        return ResponseEntity.ok(projectService.getAllProject());
    }
}
