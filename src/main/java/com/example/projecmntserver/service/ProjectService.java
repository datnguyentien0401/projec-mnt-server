package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.JiraFieldConstant.ASSIGNEE;
import static com.example.projecmntserver.constant.JiraFieldConstant.STATUS;
import static com.example.projecmntserver.constant.JiraFieldConstant.TIME_SPENT;
import static com.example.projecmntserver.type.JiraIssueType.IGNORE_SEARCH_ISSUE;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.projecmntserver.dto.jira.EpicDto;
import com.example.projecmntserver.dto.jira.IssueDto;
import com.example.projecmntserver.dto.response.EpicRemainingResponse;
import com.example.projecmntserver.dto.response.ProjectDto;
import com.example.projecmntserver.dto.response.ProjectResponse;
import com.example.projecmntserver.util.DatetimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final JiraApiService jiraApiService;

    public List<EpicDto> getAllProject(boolean groupEpic) {
        return getAllEpics(new ArrayList<>(), groupEpic);
    }

    public ProjectResponse getProjectStatistic(List<String> epicIds,
                                               LocalDate fromDate,
                                               LocalDate toDate) {
        final var projectResponse = new ProjectResponse();
        final List<ProjectDto> projectTotalDataPerMonth = new ArrayList<>();
        final List<ProjectDto> projectListDataPerMonth = new ArrayList<>();

        String jql = "";
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql = String.format(" AND 'epic link' IN ( %s )", String.join(", ", epicIds));
        }
        final List<EpicDto> allEpics = getAllEpics(epicIds, true);

        final var monthCount = ChronoUnit.MONTHS.between(fromDate, toDate) + 1;
        var initDate = fromDate;
        for (int i = 1; i <= monthCount; i++) {
            final boolean forColumnChart = i > monthCount - 3;

            projectTotalDataPerMonth.add(
                    getProjectTotalDataPerMonth(
                            jql,
                            initDate.withDayOfMonth(1),
                            initDate.withDayOfMonth(initDate.lengthOfMonth())));

            for (var epic : allEpics) {
                projectListDataPerMonth.add(
                        getProjectListPerMonth(
                                epic,
                                initDate.withDayOfMonth(1),
                                initDate.withDayOfMonth(initDate.lengthOfMonth()),
                                forColumnChart));
            }

            initDate = initDate.plusMonths(1);
        }

        projectResponse.setTotalData(projectTotalDataPerMonth);
        projectResponse.setListData(projectListDataPerMonth);

        return projectResponse;
    }

    public List<EpicDto> getAllEpics(List<String> epicIds, boolean groupEpic) {
        String jql = "issuetype = 'epic' ";
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql += String.format(" AND id IN (%s) ", String.join(",", epicIds));
        }
        final var response = jiraApiService.searchIssue(jql, "");

        final List<EpicDto> allEpics = new ArrayList<>();
        if (Objects.nonNull(response)) {
            final Map<String, EpicDto> epicGroups = new HashMap<>();
            final List<IssueDto> issues = response.getIssues();
            for (var epic : issues) {
                final String epicName = groupEpic ? getEpicPrefix(epic.getFields().getEpicName()) :
                                        epic.getFields().getEpicName();
                if (!epicGroups.containsKey(epicName)) {
                    epicGroups.put(epicName, new EpicDto());
                }
                final EpicDto epicDto = epicGroups.get(epicName);
                epicDto.getIds().add(epic.getId());
                epicDto.setName(epicName);
                epicDto.setDueDate(epic.getFields().getDueDate());
                epicDto.setStatus(epic.getFields().getStatus().getName());
                epicGroups.put(epicName, epicDto);
            }
            allEpics.addAll(epicGroups.values());
        }
        return allEpics;
    }

    private static String getEpicPrefix(String epicName) {
        return epicName.split("-")[0];
    }

    public ProjectDto getProjectTotalDataPerMonth(String jql,
                                                  LocalDate fromDate,
                                                  LocalDate toDate) {
        final var response = jiraApiService.searchIssue(
                String.format("type NOT IN ( %s ) AND updated >= %s AND updated <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql,
                String.join(",", Arrays.asList(ASSIGNEE, TIME_SPENT)));

        final var projectDto = new ProjectDto();
        projectDto.setMonth(DatetimeUtils.toMonth(fromDate));
        final Set<String> assigneeIdSet = new HashSet<>();
        if (Objects.nonNull(response)) {
            final List<IssueDto> issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();
                if (Objects.nonNull(fields.getTimeSpent())) {
                    projectDto.setTotalTimeSpent(
                            projectDto.getTotalTimeSpent() + fields.getTimeSpent());
                }
                if (Objects.nonNull(fields.getAssignee())) {
                    assigneeIdSet.add(fields.getAssignee().getAccountId());
                }
            }
        }
        projectDto.setTotalHeadCount(assigneeIdSet.size());
        getResolvedIssueDataPerMonth(projectDto, jql, fromDate, toDate);
        return projectDto;
    }

    private ProjectDto getProjectListPerMonth(EpicDto epicDto,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              boolean forColumnChart) {
        final String jql = String.format("AND 'epic link' IN ( %s )", String.join(", ", epicDto.getIds()));
        final var response = jiraApiService.searchIssue(
                String.format(" type NOT IN ( %s ) AND updated >= %s AND updated <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql,
                String.join(",", Arrays.asList(TIME_SPENT, STATUS)));

        final var projectDto = new ProjectDto();
        if (response == null) {
            return projectDto;
        }
        projectDto.setMonth(DatetimeUtils.toMonth(fromDate));
        projectDto.setEpicIds(epicDto.getIds());
        projectDto.setEpicName(epicDto.getName());
        projectDto.setForColumnChart(forColumnChart);

        final var issues = response.getIssues();
        for (var issue : issues) {
            final var fields = issue.getFields();
            if (Objects.nonNull(fields.getTimeSpent())) {
                projectDto.setTotalTimeSpent(
                        projectDto.getTotalTimeSpent() + fields.getTimeSpent());
            }

            if (forColumnChart) {
                final var status = fields.getStatus();
                if (status.getStatusCategory().getKey().isNew()) {
                    projectDto.setTotalOpenIssue(projectDto.getTotalOpenIssue() + 1);
                } else if (status.getStatusCategory().getKey().isIndeterminate()) {
                    projectDto.setTotalInProgressIssue(
                            projectDto.getTotalInProgressIssue() + 1);
                }
            }
        }

        getResolvedIssueDataPerMonth(projectDto, jql, fromDate, toDate);

        return projectDto;
    }

    private void getResolvedIssueDataPerMonth(ProjectDto project, String jql,
                                              LocalDate fromDate, LocalDate toDate) {
        final var response = jiraApiService.searchIssue(
                String.format("type NOT IN ( %s ) AND resolved >= %s AND resolved <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql, "");

        if (Objects.nonNull(response)) {
            project.setTotalResolvedIssue(response.getTotal());

            final var issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();
                if (Objects.nonNull(fields.getStoryPoint())) {
                    project.setTotalStoryPoint(
                            project.getTotalStoryPoint() + fields.getStoryPoint());
                }
            }
        }
    }

    public List<EpicRemainingResponse> getEpicRemaining(List<String> epicIds) {
        final List<EpicRemainingResponse> result = new ArrayList<>();
        final List<EpicDto> allEpics = getAllEpics(epicIds, false);
        for (var epic : allEpics) {
            final var epicRemainingResponse = new EpicRemainingResponse();
            epicRemainingResponse.setEpicName(epic.getName());
            epicRemainingResponse.setEpicIds(epic.getIds());
            epicRemainingResponse.setDueDate(epic.getDueDate());
            epicRemainingResponse.setStatus(epic.getStatus());

            final var response = jiraApiService.searchIssue(
                    String.format("type NOT IN ( %s ) AND 'epic link' IN ( %s ) ",
                                  String.join(", ", IGNORE_SEARCH_ISSUE),
                                  String.join(", ", epic.getIds())),
                    "");
            final Set<String> assigneeIdSet = new HashSet<>();
            if (Objects.nonNull(response)) {
                final var issues = response.getIssues();
                for (var issue : issues) {
                    final var fields = issue.getFields();
                    if (Objects.nonNull(fields.getTimeEstimate())) {
                        epicRemainingResponse.setTimeEstimate(
                                epicRemainingResponse.getTimeEstimate() + fields.getTimeEstimate());
                    }
                    if (Objects.nonNull(fields.getAssignee())) {
                        assigneeIdSet.add(fields.getAssignee().getAccountId());
                    }
                }
            }
            epicRemainingResponse.setHeadCount(assigneeIdSet.size());

            result.add(epicRemainingResponse);
        }
        return result;
    }
}
