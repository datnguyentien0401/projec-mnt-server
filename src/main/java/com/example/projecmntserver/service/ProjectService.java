package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.JiraFieldConstant.ASSIGNEE;
import static com.example.projecmntserver.constant.JiraFieldConstant.PROJECT;
import static com.example.projecmntserver.constant.JiraFieldConstant.STATUS;
import static com.example.projecmntserver.constant.JiraFieldConstant.TIME_ESTIMATE;
import static com.example.projecmntserver.constant.JiraFieldConstant.TIME_SPENT;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.projecmntserver.dto.ProjectStatisticByProjectIdDto;
import com.example.projecmntserver.dto.ProjectStatisticResponseDto;
import com.example.projecmntserver.dto.jira.IssueDto;
import com.example.projecmntserver.dto.jira.ProjectDto;
import com.example.projecmntserver.dto.ProjectStatisticDto;
import com.example.projecmntserver.util.DatetimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final JiraApiService jiraApiService;

    public ProjectDto[] getAllProject() {
        return jiraApiService.getAllProject();
    }

    public ProjectStatisticResponseDto getProjectStatistic(List<String> projectIds, LocalDate fromDate,
                                                           LocalDate toDate) {
        final var projectStatisticResponse = new ProjectStatisticResponseDto();
        final List<ProjectStatisticDto> projectTableData = new ArrayList<>();
        final List<ProjectStatisticDto> allProjectStatisticPerMonth = new ArrayList<>();

        final var monthCount = ChronoUnit.MONTHS.between(fromDate, toDate) + 1;
        var initDate = fromDate;
        for (int i = 1; i <= monthCount; i++) {
            final boolean isForColumnChart = i > monthCount - 3;

            projectTableData.add(getOneOrTotalProjectStatisticPerMonth(projectIds,
                                                                       initDate.withDayOfMonth(1),
                                                                       initDate.withDayOfMonth(
                                                                               initDate.lengthOfMonth())));

            allProjectStatisticPerMonth.addAll(getAllProjectStatisticPerMonth(initDate.withDayOfMonth(1),
                                                                              initDate.withDayOfMonth(
                                                                                      initDate.lengthOfMonth()),
                                                                              isForColumnChart));

            initDate = initDate.plusMonths(1);
        }

        projectStatisticResponse.setTableData(projectTableData);
        projectStatisticResponse.setChartData(allProjectStatisticPerMonth);

        return projectStatisticResponse;
    }

    public ProjectStatisticDto getOneOrTotalProjectStatisticPerMonth(List<String> projectIds, LocalDate fromDate,
                                                                     LocalDate toDate) {
        String projectJql = "";
        if (!CollectionUtils.isEmpty(projectIds)) {
            projectJql = String.format(" AND project IN ( %s )", String.join(", ", projectIds));
        }
        final var response = jiraApiService.searchProject(
                String.format("updated >= %s AND updated <= %s", fromDate, toDate) + projectJql,
                String.join(",", Arrays.asList(ASSIGNEE, TIME_SPENT)));

        final var projectStatistic = new ProjectStatisticDto();
        projectStatistic.setMonth(DatetimeUtils.toDate(fromDate, null));

        if (Objects.nonNull(response)) {
            final List<IssueDto> issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();
                if (Objects.nonNull(fields.getTimeSpent())) {
                    projectStatistic.setTotalTimeSpent(
                            projectStatistic.getTotalTimeSpent() + fields.getTimeSpent());
                }
                if (Objects.nonNull(fields.getAssignee())) {
                    projectStatistic.setTotalHeadCount(projectStatistic.getTotalHeadCount() + 1);
                }
            }
        }
        getResolvedIssueOneOrTotalProjectStatisticPerMonth(projectStatistic, projectJql, fromDate, toDate);
        return projectStatistic;
    }

    private void getResolvedIssueOneOrTotalProjectStatisticPerMonth(
            ProjectStatisticDto projectStatistic, String projectJql, LocalDate fromDate, LocalDate toDate) {
        final var response = jiraApiService.searchProject(
                String.format("resolved >= %s AND resolved <= %s", fromDate, toDate) + projectJql,
                TIME_ESTIMATE);

        if (Objects.nonNull(response)) {
            projectStatistic.setTotalResolvedIssue(response.getTotal());

            final var issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();
                if (Objects.nonNull(fields.getTimeEstimate())) {
                    projectStatistic.setTotalStoryPoint(
                            projectStatistic.getTotalStoryPoint() + fields.getTimeEstimate());
                }
            }
        }
    }

    private static List<ProjectStatisticByProjectIdDto> handleAllProjectStatisticPerMonthByProjectId(
            List<Map<String, ProjectStatisticDto>> projectStatisticPerMonths) {
        final Map<String, List<ProjectStatisticDto>> projectStatisticById = new HashMap<>();
        for (var projectStatisticPerMonth : projectStatisticPerMonths) {
            projectStatisticPerMonth.forEach((projectId, projectStatistic) -> {
                List<ProjectStatisticDto> projectStatisticByIdList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(projectStatisticById.get(projectId))) {
                    projectStatisticByIdList = projectStatisticById.get(projectId);
                }
                projectStatisticByIdList.add(projectStatistic);
                projectStatisticById.put(projectId, projectStatisticByIdList);
            });
        }
        final List<ProjectStatisticByProjectIdDto> result = new ArrayList<>();
        projectStatisticById.forEach((projectId, projectStatisticByIdList) -> {
            result.add(new ProjectStatisticByProjectIdDto(projectId, projectStatisticByIdList));
        });
        return result;
    }

    private List<ProjectStatisticDto> getAllProjectStatisticPerMonth(LocalDate fromDate,
                                                                     LocalDate toDate,
                                                                     boolean isForColumnChart) {
        final var response = jiraApiService.searchProject(
                String.format("updated >= %s AND updated <= %s", fromDate, toDate),
                String.join(",", Arrays.asList(PROJECT, TIME_SPENT, STATUS)));
        if (response == null) {
            return new ArrayList<>();
        }

        final var issues = response.getIssues();

        final Map<String, ProjectStatisticDto> projectStatisticById = new HashMap<>();
        for (var issue : issues) {
            final var fields = issue.getFields();
            final var project = fields.getProject();
            ProjectStatisticDto projectStatisticDto = new ProjectStatisticDto();
            if (Objects.nonNull(projectStatisticById.get(project.getId()))) {
                projectStatisticDto = projectStatisticById.get(project.getId());
            }
            if (Objects.nonNull(fields.getTimeSpent())) {
                projectStatisticDto.setTotalTimeSpent(
                        projectStatisticDto.getTotalTimeSpent() + fields.getTimeSpent());
            }

            final var status = fields.getStatus();
            if (isForColumnChart) {
                if (status.getStatusCategory().getKey().isNew()) {
                    projectStatisticDto.setTotalOpenIssue(projectStatisticDto.getTotalOpenIssue() + 1);
                } else if (status.getStatusCategory().getKey().isIndeterminate()) {
                    projectStatisticDto.setTotalInProgressIssue(
                            projectStatisticDto.getTotalInProgressIssue() + 1);
                }
            }
            projectStatisticDto.setMonth(DatetimeUtils.toDate(fromDate, null));
            projectStatisticDto.setProjectId(project.getId());
            projectStatisticDto.setProjectName(project.getName());
            projectStatisticById.put(project.getId(), projectStatisticDto);
        }

        getResolvedIssueAllProjectStatisticPerMonth(projectStatisticById, fromDate, toDate);

        return new ArrayList<>(projectStatisticById.values());
    }

    private void getResolvedIssueAllProjectStatisticPerMonth(
            Map<String, ProjectStatisticDto> projectStatisticById, LocalDate fromDate, LocalDate toDate) {
        final var response = jiraApiService.searchProject(
                String.format("resolved >= %s AND resolved <= %s", fromDate, toDate),
                String.join(",", Arrays.asList(PROJECT, TIME_ESTIMATE)));

        if (Objects.nonNull(response)) {
            final var issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();
                final var project = fields.getProject();
                ProjectStatisticDto projectStatisticDto = new ProjectStatisticDto();
                if (Objects.nonNull(projectStatisticById.get(project.getId()))) {
                    projectStatisticDto = projectStatisticById.get(project.getId());
                }
                projectStatisticDto.setTotalResolvedIssue(projectStatisticDto.getTotalResolvedIssue() + 1);
                if (Objects.nonNull(fields.getTimeEstimate())) {
                    projectStatisticDto.setTotalStoryPoint(
                            projectStatisticDto.getTotalStoryPoint() + fields.getTimeEstimate());
                }
                projectStatisticById.put(project.getId(), projectStatisticDto);
            }
        }
    }

}
