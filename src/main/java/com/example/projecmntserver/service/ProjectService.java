package com.example.projecmntserver.service;

import static com.example.projecmntserver.type.JiraIssueType.IGNORE_SEARCH_ISSUE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.projecmntserver.constant.Constant;
import com.example.projecmntserver.dto.jira.EpicDto;
import com.example.projecmntserver.dto.jira.IssueDto;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.response.EpicRemainingResponse;
import com.example.projecmntserver.dto.response.OverallTeamResponse;
import com.example.projecmntserver.dto.response.ProjectDto;
import com.example.projecmntserver.dto.response.ProjectResponse;
import com.example.projecmntserver.util.DatetimeUtils;
import com.example.projecmntserver.util.NumberUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final JiraApiService jiraApiService;

    public List<EpicDto> getAllEpic(List<String> jiraProjectIds, boolean groupEpic) {
        return getAllEpics(new ArrayList<>(), jiraProjectIds, groupEpic);
    }

    public ProjectResponse getProjectStatisticV2(List<String> epicIds,
                                                 LocalDate fromDate,
                                                 LocalDate toDate) {
        final var projectResponse = new ProjectResponse();

        String jql = "";
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql = String.format(" AND 'epic link' IN ( %s )", String.join(", ", epicIds));
        }

        final var response = jiraApiService.searchIssue(
                String.format(" type NOT IN ( %s ) AND updated >= %s AND updated <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql);
        if (response == null) {
            return projectResponse;
        }

        projectResponse.setTotalData(
                new ArrayList<>(
                        getProjectSumDataPerMonthV2(response.getIssues(), jql, fromDate, toDate).values())
                        .stream()
                        .sorted(Comparator.comparing(ProjectDto::getMonth))
                        .toList());

        getProjectListPerMonthV2(epicIds, fromDate, toDate)
                .forEach(projectByEpic -> projectResponse.getListData().addAll(projectByEpic.values()));

        //sort
        projectResponse.setListData(
                projectResponse.getListData()
                               .stream()
                               .sorted(Comparator.comparing(ProjectDto::getMonth))
                               .toList());

        return projectResponse;
    }

    public Map<String, ProjectDto> getProjectSumDataPerMonthV2(List<IssueDto> issues,
                                                               String jql,
                                                               LocalDate fromDate,
                                                               LocalDate toDate) {
        final Map<String, ProjectDto> projectByMonth = new HashMap<>();
        for (var issue : issues) {
            final var fields = issue.getFields();

            var projectDto = new ProjectDto();

            final String month = DatetimeUtils.toMonth(
                    DatetimeUtils.parseDatetime(fields.getUpdatedAt()).toLocalDate());

            if (projectByMonth.containsKey(month)) {
                projectDto = projectByMonth.get(month);
            } else {
                projectDto.setMonth(month);
            }

            if (Objects.nonNull(fields.getTimeSpent())) {
                projectDto.setTotalTimeSpent(
                        projectDto.getTotalTimeSpent() + fields.getTimeSpent());
            }
            if (Objects.nonNull(fields.getAssignee())) {
                projectDto.getAssignees().add(fields.getAssignee().getAccountId());
            }
            projectByMonth.put(month, projectDto);
        }
        getResolvedIssueSumDataPerMonthV2(projectByMonth, jql, fromDate, toDate);
        return projectByMonth;
    }

    private void getResolvedIssueSumDataPerMonthV2(Map<String, ProjectDto> projectByMonth, String jql,
                                                 LocalDate fromDate, LocalDate toDate) {
        final var response = jiraApiService.searchIssue(
                String.format("type NOT IN ( %s ) AND resolved >= %s AND resolved <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql);

        if (Objects.nonNull(response)) {

            final var issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();

                var project = new ProjectDto();

                final String month = DatetimeUtils.toMonth(
                        DatetimeUtils.parseDatetime(fields.getResolvedAt()).toLocalDate());

                if (projectByMonth.containsKey(month)) {
                    project = projectByMonth.get(month);
                } else {
                    project.setMonth(month);
                }
                if (Objects.nonNull(fields.getStoryPoint())) {
                    project.setTotalStoryPoint(
                            project.getTotalStoryPoint() + fields.getStoryPoint());
                }
                project.setTotalResolvedIssue(project.getTotalResolvedIssue() + 1);
                projectByMonth.put(month, project);
            }
        }
    }

    private List<Map<String, ProjectDto>> getProjectListPerMonthV2(List<String> epicIds,
                                                                   LocalDate fromDate,
                                                                   LocalDate toDate) {
        final List<Map<String, ProjectDto>> projectByMonthList = new ArrayList<>();
        final List<EpicDto> allEpics = getAllEpics(epicIds, new ArrayList<>(), true);

        for (var epic : allEpics) {
            final String jql = String.format("AND 'epic link' IN ( %s )", String.join(", ", epic.getIds()));
            final var response = jiraApiService.searchIssue(
                    String.format(" type NOT IN ( %s ) AND updated >= %s AND updated <= %s ",
                                  String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql);
            if (Objects.isNull(response)) {
                continue;
            }

            final Map<String, ProjectDto> projectByMonth = new HashMap<>();

            for (var issue : response.getIssues()) {
                final var fields = issue.getFields();

                final LocalDate updatedDate = DatetimeUtils.parseDatetime(fields.getUpdatedAt()).toLocalDate();
                final String month = DatetimeUtils.toMonth(updatedDate);

                var project = new ProjectDto();
                if (projectByMonth.containsKey(month)) {
                    project = projectByMonth.get(month);
                }

                project.setMonth(month);
                project.setEpicIds(epic.getIds());
                project.setEpicName(epic.getName());

                if (Objects.nonNull(fields.getTimeSpent())) {
                    project.setTotalTimeSpent(
                            project.getTotalTimeSpent() + fields.getTimeSpent());
                }

                if (DatetimeUtils.countMonth(updatedDate, toDate) <= 3) {
                    project.setForColumnChart(true);
                    final var status = fields.getStatus();
                    if (status.getStatusCategory().getKey().isNew()) {
                        project.setTotalOpenIssue(project.getTotalOpenIssue() + 1);
                    } else if (status.getStatusCategory().getKey().isIndeterminate()) {
                        project.setTotalInProgressIssue(
                                project.getTotalInProgressIssue() + 1);
                    }
                }

                projectByMonth.put(month, project);
            }
            getResolvedIssueDataPerMonthV2(projectByMonth, jql, fromDate, toDate);

            projectByMonthList.add(projectByMonth);
        }

        return projectByMonthList;
    }

    private void getResolvedIssueDataPerMonthV2(Map<String, ProjectDto> projectByMonth, String jql,
                                              LocalDate fromDate, LocalDate toDate) {
        final var response = jiraApiService.searchIssue(
                String.format("type NOT IN ( %s ) AND resolved >= %s AND resolved <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql);
        if (Objects.nonNull(response)) {
            final var issues = response.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();

                final String month = DatetimeUtils.toMonth(
                        DatetimeUtils.parseDatetime(fields.getResolvedAt()).toLocalDate());

                if (projectByMonth.containsKey(month)) {
                    final var project = projectByMonth.get(month);
                    if (Objects.nonNull(fields.getStoryPoint())) {
                        project.setTotalStoryPoint(
                                project.getTotalStoryPoint() + fields.getStoryPoint());
                    }
                    project.setTotalResolvedIssue(project.getTotalResolvedIssue() + 1);

                    projectByMonth.put(month, project);
                }
            }
        }
    }

    public List<EpicDto> getAllEpics(List<String> epicIds, List<String> jiraProjectIds, boolean groupEpic) {
        String jql = "issuetype = 'epic' ";
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql += String.format(" AND id IN (%s) ", String.join(",", epicIds));
        } else if (!CollectionUtils.isEmpty(jiraProjectIds)) {
            jql += String.format(" AND project IN (%s) ", String.join(",", jiraProjectIds));
        }
        final var response = jiraApiService.searchIssue(jql);

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

    public List<EpicRemainingResponse> getEpicRemaining(List<String> epicIds) {
        final List<EpicRemainingResponse> result = new ArrayList<>();
        final List<EpicDto> allEpics = getAllEpics(epicIds, new ArrayList<>(), false);
        for (var epic : allEpics) {
            final var epicRemainingResponse = new EpicRemainingResponse();
            epicRemainingResponse.setEpicName(epic.getName());
            epicRemainingResponse.setEpicIds(epic.getIds());
            epicRemainingResponse.setDueDate(epic.getDueDate());
            epicRemainingResponse.setStatus(epic.getStatus());

            final var response = jiraApiService.searchIssue(
                    String.format("type NOT IN ( %s ) AND 'epic link' IN ( %s ) ",
                                  String.join(", ", IGNORE_SEARCH_ISSUE),
                                  String.join(", ", epic.getIds())));
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

    public OverallTeamResponse getTeamOverall(LocalDate fromDate, LocalDate toDate,
                                              List<String> jiraMemberIds) {
        final OverallTeamResponse result = new OverallTeamResponse();
        getTeamOverallResolvedIssue(result, fromDate, toDate, jiraMemberIds);
        getTeamOverallTimeSpentAndStoryPoint(result, fromDate, toDate, jiraMemberIds);
        return result;
    }

    private void getTeamOverallResolvedIssue(OverallTeamResponse result,
                                             LocalDate fromDate, LocalDate toDate,
                                             List<String> jiraMemberIds) {
        final var IssueSearchRes = getIssuesByAssignee(fromDate, toDate, jiraMemberIds, "resolved");

        if (Objects.nonNull(IssueSearchRes)) {
            final long monthCount = DatetimeUtils.countMonth(fromDate, toDate);
            final var totalResolvedIssue = IssueSearchRes.getTotal();
            result.setTotalResolvedIssue(totalResolvedIssue);
            result.setAvgResolvedIssue(
                    NumberUtils.round((double) totalResolvedIssue / (monthCount * jiraMemberIds.size())));
        }
    }

    private void getTeamOverallTimeSpentAndStoryPoint(OverallTeamResponse result,
                                                      LocalDate fromDate, LocalDate toDate,
                                                      List<String> jiraMemberIds) {
        final var IssueSearchRes = getIssuesByAssignee(fromDate, toDate, jiraMemberIds, "updated");

        double totalTimeSpent = 0.0;
        double totalStoryPoint = 0.0;
        final long monthCount = DatetimeUtils.countMonth(fromDate, toDate);

        if (Objects.nonNull(IssueSearchRes)) {
            final var issues = IssueSearchRes.getIssues();
            for (var issue : issues) {
                final var fields = issue.getFields();
                if (Objects.nonNull(fields.getTimeSpent())) {
                    totalTimeSpent += fields.getTimeSpent();
                }
                if (Objects.nonNull(fields.getStoryPoint())) {
                    totalStoryPoint += fields.getStoryPoint();
                }
            }
        }
        result.setAvgTimeSpent(NumberUtils.round(totalTimeSpent / Constant.TIME_MM / (monthCount * jiraMemberIds.size())));
        result.setAvgStoryPoint(NumberUtils.round(totalStoryPoint / (monthCount * jiraMemberIds.size())));
    }

    public IssueSearchResponse getIssuesByAssignee(LocalDate fromDate, LocalDate toDate,
                                                   List<String> jiraMemberIds, String dateFieldName) {
        String jql = String.format("type NOT IN ( %s ) AND %s >= %s AND %s <= %s ",
                                   String.join(", ", IGNORE_SEARCH_ISSUE),
                                   dateFieldName, fromDate,
                                   dateFieldName, toDate);
        if (!CollectionUtils.isEmpty(jiraMemberIds)) {
            jql += String.format("AND assignee IN ( %s )", String.join(",", jiraMemberIds));
        }
        return jiraApiService.searchIssue(jql);
    }

    public List<JiraProjectDto> getJiraProject(String jiraProjectName) {
        final var response = jiraApiService.getAllProject(jiraProjectName);
        if (response == null) {
            return new ArrayList<>();
        }
        return List.of(response);
    }
}
