package com.example.projecmntserver.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.projecmntserver.dto.jira.EpicDto;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.response.EpicRemainingResponse;
import com.example.projecmntserver.dto.response.ProjectResponse;
import com.example.projecmntserver.service.ProjectService;
import com.example.projecmntserver.type.ProjectSearchType;
import com.example.projecmntserver.util.DatetimeUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@Validated
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/search")
    public ResponseEntity<List<JiraProjectDto>> search(
            @RequestParam(required = false) String jiraProjectName) {
        return ResponseEntity.ok(projectService.getJiraProject(jiraProjectName));
    }

    @GetMapping("/epic/search")
    public ResponseEntity<ProjectResponse> search(
            @RequestParam List<String> epicIds,
            @RequestParam(defaultValue = "RESOLVED_ISSUE") ProjectSearchType type,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return ResponseEntity.ok(projectService.getProjectStatisticV2(getEpicIds(epicIds),
                                                                      type,
                                                                      DatetimeUtils.parse(fromDate),
                                                                      DatetimeUtils.parse(toDate)));
    }

    @GetMapping("/jira-project/search")
    public ResponseEntity<ProjectResponse> jiraProjectSearch(
            @RequestParam List<String> jiraProjectIds,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return ResponseEntity.ok(projectService.getJiraProjectStatistic(jiraProjectIds,
                                                                        DatetimeUtils.parse(fromDate),
                                                                        DatetimeUtils.parse(toDate)));
    }

    private static List<String> getEpicIds(List<String> projectIds) {
        final List<String> epicIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(projectIds)) {
            for (var projectId : projectIds) {
                final String[] epicIdArr = projectId.split("-");
                if (epicIdArr.length > 0) {
                    epicIds.addAll(Arrays.asList(epicIdArr));
                }
            }
        }
        return epicIds;
    }

    @GetMapping("/remaining")
    public ResponseEntity<List<EpicRemainingResponse>> remaining(
            @RequestParam(required = false) List<String> projectIds) {
        return ResponseEntity.ok(projectService.getEpicRemaining(getEpicIds(projectIds)));
    }

    @GetMapping("/epic")
    public ResponseEntity<List<EpicDto>> getAll(
            @RequestParam List<String> jiraProjectIds,
            @RequestParam(required = false, defaultValue = "true") Boolean groupEpic,
            @RequestParam(required = false, defaultValue = "false") Boolean resolvedEpic) {
        return ResponseEntity.ok(projectService.getAllEpics(jiraProjectIds, groupEpic, resolvedEpic));
    }
}
