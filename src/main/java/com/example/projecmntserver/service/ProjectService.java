package com.example.projecmntserver.service;

import static com.example.projecmntserver.type.JiraIssueType.IGNORE_SEARCH_ISSUE_TYPE;
import static com.example.projecmntserver.type.JiraStatus.DONE_STATUS_LIST;
import static com.example.projecmntserver.type.JiraStatus.IN_PROGRESS_STATUS_LIST;
import static com.example.projecmntserver.type.JiraStatus.OPEN_STATUS_LIST;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.projecmntserver.constant.Constant;
import com.example.projecmntserver.constant.JiraFieldConstant;
import com.example.projecmntserver.domain.Member;
import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.jira.ChangelogDto;
import com.example.projecmntserver.dto.jira.EpicDto;
import com.example.projecmntserver.dto.jira.FieldDto;
import com.example.projecmntserver.dto.jira.HistoryDto;
import com.example.projecmntserver.dto.jira.HistoryItemDto;
import com.example.projecmntserver.dto.jira.IssueDto;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.IssueTypeDto;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.jira.ParentDto;
import com.example.projecmntserver.dto.jira.ParentFieldDto;
import com.example.projecmntserver.dto.jira.WorkLogDto;
import com.example.projecmntserver.dto.response.EpicRemainingResponse;
import com.example.projecmntserver.dto.response.OverallTeamResponse;
import com.example.projecmntserver.dto.response.ProjectDto;
import com.example.projecmntserver.dto.response.ProjectResponse;
import com.example.projecmntserver.type.ProjectSearchType;
import com.example.projecmntserver.util.DatetimeUtils;
import com.example.projecmntserver.util.Helper;
import com.example.projecmntserver.util.NumberUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private static final String[] ISSUE_FIELDS = {
        "project",
        "assignee",
        "status",
        "summary",
        "customfield_10308",
        "timeestimate",
        "timespent",
        "duedate",
        "updated",
        "updatedAt",
        "resolutiondate",
        "resolvedAt",
        "parent",
        "worklog",
        "created"
    };

    private final JiraApiService jiraApiService;

    public List<EpicDto> getAllEpics(List<String> jiraProjectIds, boolean groupEpic, boolean resolvedEpic) {
        return getAllEpics(new ArrayList<>(), new ArrayList<>(), jiraProjectIds, groupEpic, resolvedEpic);
    }

    private static String buildJql(List<String> epicIds) {
        final var jql = new StringBuilder(
                String.format("type NOT IN (%s) ", String.join(",", IGNORE_SEARCH_ISSUE_TYPE)));
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql.append(
                    String.format(" AND 'epic link' IN (%s)", String.join(",", epicIds)));
        }
        return jql.toString();
    }

    private List<String> getAllEpicIds(List<String> jiraProjectIds) {
        final List<EpicDto> allEpics = getAllEpics(jiraProjectIds, false, true);
        final List<String> allEpicIds = new ArrayList<>();
        allEpics.forEach(e -> allEpicIds.addAll(e.getIds()));
        return allEpicIds;
    }

    public ProjectResponse getJiraProjectStatistic(List<String> jiraProjectIds,
                                                   LocalDate fromDate,
                                                   LocalDate toDate,
                                                   ProjectSearchType searchType) {
        final var projectResponse = new ProjectResponse();

        final List<JiraProjectDto> jiraProjects = getJiraProject(jiraProjectIds);

        final List<String> allEpicIds = getAllEpicIds(jiraProjectIds);

        final List<IssueDto> allIssues = getChildIssues(allEpicIds);

        final Map<String, List<IssueDto>> issuesByJiraProject = new HashMap<>();

        allIssues.parallelStream().forEach(issue -> {
            final JiraProjectDto project = issue.getFields().getProject();
            if (Objects.nonNull(project)) {
                final String projectId = project.getId();
                issuesByJiraProject.putIfAbsent(projectId, new ArrayList<>());
                issuesByJiraProject.get(projectId).add(issue);
            }
        });

        final List<ProjectDto> projectListPerMonth = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(10);

        jiraProjects.parallelStream().forEach(jiraProject -> {
            executor.submit(() -> {
                handleJiraProjectSumData(projectListPerMonth, issuesByJiraProject,
                                         jiraProject, fromDate, toDate, searchType);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        projectResponse.setListData(projectListPerMonth);

        projectResponse.setTotalData(
                new ArrayList<>(
                        getProjectSumDataPerMonthV2(allIssues, fromDate, toDate, true).values())
                        .stream()
                        .sorted(Comparator.comparing(ProjectDto::getMonth))
                        .toList());

        return projectResponse;
    }

    private void handleJiraProjectSumData(List<ProjectDto> projectListPerMonth,
                                          Map<String, List<IssueDto>> issuesByJiraProject,
                                          JiraProjectDto jiraProject,
                                          LocalDate fromDate,
                                          LocalDate toDate,
                                          ProjectSearchType searchType) {
        final String jiraProjectId = jiraProject.getId();

        final List<IssueDto> issues = issuesByJiraProject.getOrDefault(jiraProjectId, new ArrayList<>());
        if (CollectionUtils.isEmpty(issues)) {
            return ;
        }
        projectListPerMonth.addAll(getProjectSumDataPerMonthV2(issues, fromDate, toDate, false, searchType, jiraProject)
                                           .values()
                                           .stream()
                                           .sorted(Comparator.comparing(ProjectDto::getMonth))
                                           .toList());
    }

    private static String buildGetEpicIssuesJql(List<String> epicIds) {
        String jql = "issuetype = 'epic' ";
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql += String.format(" AND id IN (%s) ", String.join(",", epicIds));
        }
        return jql;
    }

    private List<IssueDto> getChildIssues(List<String> epicIds) {
        final String jql = buildJql(epicIds);
        final var response = jiraApiService.searchIssueExpand(jql, "changelog", ISSUE_FIELDS);
        return Objects.isNull(response) ? Collections.emptyList() : response.getIssues();
    }

    private List<IssueDto> getChildIssuesNoExpand(List<String> epicIds) {
        final String jql = buildJql(epicIds);
        final var response = jiraApiService.searchIssue(jql, ISSUE_FIELDS);
        return Objects.isNull(response) ? Collections.emptyList() : response.getIssues();
    }

    private List<IssueDto> getEpicIssues(List<String> epicIds) {
        final String jql = buildGetEpicIssuesJql(epicIds);
        final var response = jiraApiService.searchIssue(jql, ISSUE_FIELDS);
        return Objects.isNull(response) ? Collections.emptyList() : response.getIssues();
    }

    public ProjectResponse getProjectStatisticV2(List<String> epicIds,
                                                 ProjectSearchType type,
                                                 LocalDate fromDate,
                                                 LocalDate toDate) {
        final var projectResponse = new ProjectResponse();

        final List<IssueDto> childIssues = getChildIssues(epicIds);
        final List<IssueDto> epicIssues = getEpicIssues(epicIds);

        projectResponse.setTotalData(
                new ArrayList<>(
                        getProjectSumDataPerMonthV2(childIssues, fromDate, toDate, true).values())
                        .stream()
                        .sorted(Comparator.comparing(ProjectDto::getMonth))
                        .toList());

        getProjectListPerMonthV2(childIssues, epicIssues, epicIds, type, fromDate, toDate)
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
                                                               LocalDate fromDate,
                                                               LocalDate toDate,
                                                               boolean calculateTotalEpic) {
        return getProjectSumDataPerMonthV2(issues, fromDate, toDate, calculateTotalEpic, null, null);
    }

    public Map<String, ProjectDto> getProjectSumDataPerMonthV2(List<IssueDto> issues,
                                                               LocalDate fromDate,
                                                               LocalDate toDate,
                                                               boolean calculateTotalEpic,
                                                               @Nullable ProjectSearchType searchType,
                                                               JiraProjectDto jiraProject) {
        final Map<String, ProjectDto> projectByMonth = new HashMap<>();
        issues.parallelStream().forEach(issue -> {
            final var fields = issue.getFields();

            var tempDate = fromDate;
            while (!tempDate.isAfter(toDate)) {
                var projectDto = new ProjectDto();

                final String month = DatetimeUtils.toMonth(tempDate);
                final String issueCreatedMonth = DatetimeUtils.toMonth(fields.getCreated());

                if (projectByMonth.containsKey(month)) {
                    projectDto = projectByMonth.get(month);
                } else {
                    projectDto.setMonth(month);
                }

                if (YearMonth.parse(month).isBefore(YearMonth.parse(issueCreatedMonth))) {//neu month hien tai dang xet truoc month tao issue thi bo qua
                    projectByMonth.put(month, projectDto);
                    tempDate = tempDate.plusMonths(1);
                    continue;
                }

                if ((Objects.nonNull(searchType) && searchType.isTimeSpent() || calculateTotalEpic)
                    && Objects.nonNull(fields.getTimeSpent())) {
                    calculateTimeSpent(projectDto, month, fields.getWorklog());
                }
                if (calculateTotalEpic && Objects.nonNull(fields.getAssignee())) {
                    projectDto.getAssignees().add(fields.getAssignee().getAccountId());
                }
                if (!calculateTotalEpic && DatetimeUtils.countMonth(tempDate, toDate) <= Constant.COLUMN_CHART_MONTH_DISPLAY_NUM) {
                    handleIssueChartData(projectDto, issue, month, fromDate, toDate);
                }
                if (Objects.nonNull(jiraProject)) {
                    projectDto.setJiraProjectId(jiraProject.getId());
                    projectDto.setJiraProjectName(jiraProject.getName());
                }
                projectByMonth.put(month, projectDto);
                tempDate = tempDate.plusMonths(1);
            }
        });
        final List<IssueDto> resolvedIssues = Helper.getResolvedIssuesInRange(issues, fromDate, toDate);
        getResolvedIssueSumDataPerMonthV2(
                projectByMonth, resolvedIssues,
                Objects.nonNull(searchType) && searchType.isStoryPoint() || calculateTotalEpic);
        return projectByMonth;
    }

    private static void getResolvedIssueSumDataPerMonthV2(Map<String, ProjectDto> projectByMonth,
                                                          List<IssueDto> issues, boolean calculateStoryPoint) {
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
            if (calculateStoryPoint && Objects.nonNull(fields.getStoryPoint())) {
                project.setTotalStoryPoint(
                    project.getTotalStoryPoint() + fields.getStoryPoint());
            }
            project.setTotalResolvedIssue(project.getTotalResolvedIssue() + 1);
            projectByMonth.put(month, project);
        }
    }

    private List<Map<String, ProjectDto>> getProjectListPerMonthV2(List<IssueDto> childIssues,
                                                                   List<IssueDto> epicIssues,
                                                                   List<String> epicIds,
                                                                   ProjectSearchType type,
                                                                   LocalDate fromDate,
                                                                   LocalDate toDate) {
        final List<EpicDto> allEpics = getAllEpics(epicIssues, epicIds, new ArrayList<>(), true, true);

        final Map<String, Map<String, ProjectDto>> projectByEpic = new HashMap<>();
        final Map<String, EpicDto> epicMap = new HashMap<>();
        allEpics.forEach(epicDto -> {
            projectByEpic.put(epicDto.getName(), new HashMap<>());
            epicMap.put(epicDto.getName(), epicDto);
        });

        if (childIssues.isEmpty()) {
            return new ArrayList<>();
        }

        for (var issue : childIssues) {
            final var fields = issue.getFields();

            final String projectName = getProjectName(fields);
            if (!StringUtils.hasText(projectName)) {
                continue;
            }

            Map<String, ProjectDto> projectByMonth = new HashMap<>();
            if (projectByEpic.containsKey(projectName)) {
                projectByMonth = projectByEpic.get(projectName);
            }

            var tempDate = fromDate;
            while (!tempDate.isAfter(toDate)) {
                var project = new ProjectDto();

                final String month = DatetimeUtils.toMonth(tempDate);
                if (projectByMonth.containsKey(month)) {
                    project = projectByMonth.get(month);
                } else {
                    project.setMonth(month);
                    project.setEpicIds(epicMap.get(projectName).getIds());
                    project.setEpicName(projectName);
                }

                final String issueCreatedMonth = DatetimeUtils.toMonth(fields.getCreated());

                if (YearMonth.parse(month).isBefore(YearMonth.parse(issueCreatedMonth))) {//neu month hien tai dang xet truoc month tao issue thi bo qua
                    projectByMonth.put(month, project);
                    tempDate = tempDate.plusMonths(1);
                    continue;
                }

                if (DatetimeUtils.countMonth(tempDate, toDate) <= Constant.COLUMN_CHART_MONTH_DISPLAY_NUM) {
                    handleIssueChartData(project, issue, month, fromDate, toDate);
                }

                if (type == ProjectSearchType.TIME_SPENT_MM || type == ProjectSearchType.TIME_SPENT_MD) {
                    calculateTimeSpent(project, month, fields.getWorklog());
                }
                projectByMonth.put(month, project);
                tempDate = tempDate.plusMonths(1);
            }
            projectByEpic.put(projectName, projectByMonth);
        }

        final List<IssueDto> resolvedIssues = Helper.getResolvedIssuesInRange(childIssues, fromDate, toDate);
        getResolvedIssueDataPerMonthV2(projectByEpic, epicMap, type, resolvedIssues);

        return new ArrayList<>(projectByEpic.values());
    }

    private static void handleIssueChartData(@NotNull ProjectDto project, @NotNull IssueDto issue, @NotNull String month,
                                             @NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
        project.setForColumnChart(true);
        final Map<String, HistoryItemDto> statusUpdatedHistoryByMonth = getStatusUpdatedHistoryPerMonth(
                issue.getChangelog(), fromDate, toDate); //map da sap xep theo thu tu moi den cu

        if (CollectionUtils.isEmpty(statusUpdatedHistoryByMonth)) {
            final var status = issue.getFields().getStatus();
            calculateIssue(project, status.getName());
        } else {
            if (statusUpdatedHistoryByMonth.containsKey(month)) {
                final var historyItem = statusUpdatedHistoryByMonth.get(month);
                calculateIssue(project, historyItem.getToString());
            } else {
                final List<String> latestUpdatedMonths = statusUpdatedHistoryByMonth
                        .keySet().stream()
                        .filter(key -> YearMonth.parse(key).isBefore(YearMonth.parse(month)))
                        .limit(1).toList(); //tim month gan nhat de lay stt trong list month da duoc sap xep

                if (!CollectionUtils.isEmpty(latestUpdatedMonths)) {
                    final String latestMonth = latestUpdatedMonths.get(0);
                    final var historyItem = statusUpdatedHistoryByMonth.get(latestMonth);
                    calculateIssue(project, historyItem.getToString());
                }
            }
        }
    }

    private static void calculateTimeSpent(ProjectDto project, String curMonth, WorkLogDto workLogDto) {
        if (Objects.isNull(workLogDto)) {
            return;
        }
        final AtomicReference<Long> totalTimeSpent = new AtomicReference<>(project.getTotalTimeSpent());
        final List<WorkLogDto.Log> workLogs = workLogDto.getWorklogs();
        if (!CollectionUtils.isEmpty(workLogs)) {
            workLogs.forEach(wl -> {
                if (curMonth.equals(DatetimeUtils.toMonth(wl.getStarted()))) {
                    totalTimeSpent.updateAndGet(v -> v + wl.getTimeSpentSeconds());
                }
            });
        }
        project.setTotalTimeSpent(totalTimeSpent.get());
    }

    private static void calculateIssue(ProjectDto project, String status) {
        if (OPEN_STATUS_LIST.contains(status.toLowerCase())) {
            project.setTotalOpenIssue(project.getTotalOpenIssue() + 1);
        } else if (IN_PROGRESS_STATUS_LIST.contains(status.toLowerCase())) {
            project.setTotalInProgressIssue(project.getTotalInProgressIssue() + 1);
        }
    }

    private static Map<String, HistoryItemDto> getStatusUpdatedHistoryPerMonth(ChangelogDto changelog,
                                                                               LocalDate fromDate,
                                                                               LocalDate toDate) {
        if (Objects.isNull(changelog)) {
            return new HashMap<>();
        }
        List<HistoryDto> histories = changelog.getHistories();

        histories = histories.stream().filter(historyDto -> {
            final LocalDate historyUpdatedDate = DatetimeUtils.dateToLocalDate(historyDto.getCreated());
            return !fromDate.isAfter(historyUpdatedDate) && !toDate.isBefore(historyUpdatedDate);
        }).peek(history -> {
            final List<HistoryItemDto> updateStatusItems = history.getItems().stream()
                                                                  .filter(i -> JiraFieldConstant.STATUS.equals(i.getField()))
                                                                  .collect(Collectors.toList());
            history.setItems(updateStatusItems);
        }).collect(Collectors.toList());

        final Map<String, HistoryItemDto> statusUpdatedHistoryByMonth = new LinkedHashMap<>(); //map chua history update status theo tung thang
        for (var history: histories) {
            final String updatedMonth = DatetimeUtils.toMonth(history.getCreated());
            if (statusUpdatedHistoryByMonth.containsKey(updatedMonth) || CollectionUtils.isEmpty(history.getItems())) {
                continue; //bo qua vi chi can lay update history moi nhat cua month
            }
            statusUpdatedHistoryByMonth.put(updatedMonth, history.getItems().get(0));
        }
        return statusUpdatedHistoryByMonth;
    }

    private static String getProjectName(FieldDto fields, boolean groupEpic) {
        final ParentDto parent = fields.getParent();
        if (Objects.isNull(parent)) {
            return Constant.EMPTY_STRING;
        }
        final ParentFieldDto parentFields = parent.getFields();
        if (Objects.isNull(parentFields)) {
            return Constant.EMPTY_STRING;
        }
        final IssueTypeDto parentType = parentFields.getIssueType();
        if (Objects.nonNull(parentType) && "Epic".equals(parentType.getName())) {
            return groupEpic ? getEpicPrefix(parentFields.getSummary()) : parentFields.getSummary();
        }
        return Constant.EMPTY_STRING;
    }

    private static String getProjectName(FieldDto fields) {
        return getProjectName(fields, true);
    }

    private static void getResolvedIssueDataPerMonthV2(Map<String, Map<String, ProjectDto>> projectByEpic,
                                                       Map<String, EpicDto> epicMap, ProjectSearchType type,
                                                       List<IssueDto> issues) {
        for (var issue : issues) {
            final var fields = issue.getFields();

            final String projectName = getProjectName(fields);
            if (!StringUtils.hasText(projectName)) {
                continue;
            }

            Map<String, ProjectDto> projectByMonth = new HashMap<>();
            if (projectByEpic.containsKey(projectName)) {
                projectByMonth = projectByEpic.get(projectName);
            }

            final String month = DatetimeUtils.toMonth(
                DatetimeUtils.parseDatetime(fields.getResolvedAt()).toLocalDate());

            var project = new ProjectDto();
            if (projectByMonth.containsKey(month)) {
                project = projectByMonth.get(month);
            } else {
                project.setMonth(month);
                project.setEpicIds(epicMap.get(projectName).getIds());
                project.setEpicName(projectName);
            }
                project.setTotalResolvedIssue(project.getTotalResolvedIssue() + 1);

                if (type == ProjectSearchType.STORY_POINT && Objects.nonNull(fields.getStoryPoint())) {
                project.setTotalStoryPoint(
                    project.getTotalStoryPoint() + fields.getStoryPoint());
            }

            projectByMonth.put(month, project);
            projectByEpic.put(projectName, projectByMonth);
        }
    }

    public List<EpicDto> getAllEpics(List<IssueDto> epicIssues, List<String> epicIds,
                                     List<String> jiraProjectIds, boolean groupEpic, boolean resolvedEpic) {
        final List<IssueDto> issues;
        if (CollectionUtils.isEmpty(epicIssues)) {
            String jql = "issuetype = 'epic' ";
            if (!CollectionUtils.isEmpty(epicIds)) {
                jql += String.format(" AND id IN (%s)", String.join(",", epicIds));
            } else if (!CollectionUtils.isEmpty(jiraProjectIds)) {
                jql += String.format(" AND project IN (%s)", String.join(",", jiraProjectIds));
            }
            if (!resolvedEpic) {
                jql += String.format(" AND status NOT IN (%s) ",
                    String.join(",", DONE_STATUS_LIST));
            }
            final var response = jiraApiService.searchIssue(jql);
            if (Objects.nonNull(response)) {
                issues = response.getIssues();
            } else {
                issues = new ArrayList<>();
            }
        } else {
            issues = epicIssues;
        }
        final Map<String, EpicDto> epicGroups = new HashMap<>();
        for (var epic : issues) {
            final String epicName = groupEpic ? getEpicPrefix(epic.getFields().getSummary()) :
                                    epic.getFields().getSummary();
            epicGroups.computeIfAbsent(epicName, key -> new EpicDto());
            final EpicDto epicDto = epicGroups.get(epicName);
            epicDto.getIds().add(epic.getId());
            epicDto.setName(epicName);
            epicDto.setKey(epic.getKey());
            epicDto.setDueDate(epic.getFields().getDueDate());
            epicDto.setStatus(epic.getFields().getStatus().getName());
            epicGroups.put(epicName, epicDto);
        }
        return new ArrayList<>(epicGroups.values());
    }

    private static String getEpicPrefix(String epicName) {
        return StringUtils.hasText(epicName) ? StringUtils.trimWhitespace(epicName.split("_")[0]) : Constant.EMPTY_STRING;
    }

    public List<EpicRemainingResponse> getEpicRemaining(List<String> epicIds) {
        final List<EpicRemainingResponse> result = new ArrayList<>();
        final List<EpicDto> allEpics = getAllEpics(new ArrayList<>(), epicIds, new ArrayList<>(), false, false);

        final Map<String, List<IssueDto>> issuesByEpic = allEpics.stream().collect(
                Collectors.toMap(EpicDto::getName, p -> new ArrayList<>()));

        getChildIssuesNoExpand(epicIds).forEach(issue -> {
            final String epicName = getProjectName(issue.getFields(), false);
            if (issuesByEpic.containsKey(epicName)) {
                issuesByEpic.get(epicName).add(issue);
            }
        });

        long totalET = 0L;
        int totalAssignee = 0;
        for (var epic : allEpics) {
            final var epicRemainingResponse = new EpicRemainingResponse();
            epicRemainingResponse.setEpicName(epic.getName());
            epicRemainingResponse.setEpicIds(epic.getIds());
            epicRemainingResponse.setDueDate(epic.getDueDate());
            epicRemainingResponse.setStatus(epic.getStatus());

            final var issuesInEpic = issuesByEpic.get(epic.getName());
            final Set<String> assigneeIdSet = new HashSet<>();
            for (var issue : issuesInEpic) {
                final var fields = issue.getFields();
                final Long timeEstimate = fields.getTimeEstimate();
                if (Objects.nonNull(timeEstimate)) {
                    epicRemainingResponse.setTimeEstimate(
                            epicRemainingResponse.getTimeEstimate() + timeEstimate);
                    totalET += timeEstimate;
                }
                if (Objects.nonNull(fields.getAssignee())) {
                    assigneeIdSet.add(fields.getAssignee().getAccountId());
                }
            }
            final int assigneeCount = assigneeIdSet.size();
            epicRemainingResponse.setHeadCount(assigneeCount);
            totalAssignee += assigneeCount;

            result.add(epicRemainingResponse);
        }
//        if (allEpics.size() > 1) {
//            addTotalRemainingResponse(result, totalET, totalAssignee);
//        }
        return result;
    }

    private static void addTotalRemainingResponse(List<EpicRemainingResponse> epicRemainings, long totalET,
                                                  int totalAssignee) {
        if (CollectionUtils.isEmpty(epicRemainings)) {
            return;
        }
        final EpicRemainingResponse epicRemainingTotal = new EpicRemainingResponse();
        epicRemainingTotal.setTimeEstimate(totalET);
        epicRemainingTotal.setHeadCount(totalAssignee);
        epicRemainingTotal.setEpicName(getEpicPrefix(epicRemainings.get(0).getEpicName()));
        epicRemainings.add(epicRemainingTotal);
    }

    public List<OverallTeamResponse> getOverall(LocalDate fromDate, LocalDate toDate, List<Team> allTeams,
                                                Map<Long, @NotNull List<Member>> teamMembers) {
        final List<OverallTeamResponse> result = new ArrayList<>();

        final var jiraMemberIds = teamMembers.values().stream()
                                             .flatMap(List::stream)
                                             .map(Member::getJiraMemberId)
                                             .distinct()
                                             .toList();
        final IssueSearchResponse issueSearchResponse = getTeamViewIssues(fromDate, toDate, jiraMemberIds);
        if (Objects.isNull(issueSearchResponse)) {
            return result;
        }

        final var issuesByAssignee = issueSearchResponse.getIssues().stream()
            .collect(Collectors.groupingBy(issueDto -> Objects.nonNull(issueDto.getFields().getAssignee())
                                                       ? issueDto.getFields().getAssignee().getAccountId()
                                                       : Constant.EMPTY_ASSIGNEE));

        for (var team : allTeams) {
            final var members = teamMembers.get(team.getId());
            if (members.isEmpty()) {
                continue;
            }
            final List<IssueDto> issues = members.stream().map(member -> issuesByAssignee.get(member.getJiraMemberId()))
                                                 .filter(Objects::nonNull)
                                                 .flatMap(List::stream).toList();
            final OverallTeamResponse teamResponse = new OverallTeamResponse();
            teamResponse.setTeam(team.getName());
            setTeamOverallResolvedIssueAndStoryPointData(teamResponse, issues, members, fromDate, toDate);
            setTeamOverallTimeSpent(teamResponse, issues, members, fromDate, toDate);
            result.add(teamResponse);
        }

        return result;
    }

    private static void setTeamOverallResolvedIssueAndStoryPointData(OverallTeamResponse teamResponse,
                                                                     List<IssueDto> issues,
                                                                     List<Member> members, LocalDate fromDate,
                                                                     LocalDate toDate) {
        final List<IssueDto> resolvedIssues = Helper.getResolvedIssuesInRange(issues, fromDate, toDate);
        final long monthCount = DatetimeUtils.countMonthConsideringToday(fromDate, toDate);
        final var totalResolvedIssue = resolvedIssues.size();
        teamResponse.setTotalResolvedIssue(totalResolvedIssue);
        teamResponse.setAvgResolvedIssue(NumberUtils.round((double) totalResolvedIssue / (monthCount * members.size())));
        double storyPoints = 0d;
        for (var issue : resolvedIssues) {
            final var fields = issue.getFields();
            if (Objects.nonNull(fields.getStoryPoint())) {
                storyPoints += fields.getStoryPoint();
            }
        }
        teamResponse.setAvgStoryPoint(NumberUtils.round(storyPoints / (monthCount * members.size())));
    }

    private static void setTeamOverallTimeSpent(OverallTeamResponse teamResponse,
                                                List<IssueDto> issues, List<Member> members, LocalDate fromDate,
                                                LocalDate toDate) {
        double totalTimeSpent = 0.0;
        final long monthCount = DatetimeUtils.countMonthConsideringToday(fromDate, toDate);
        for (IssueDto issue : issues) {
            final var fields = issue.getFields();
            final WorkLogDto workLog = fields.getWorklog();
            if (Objects.nonNull(workLog)) {
                final List<WorkLogDto.Log> workLogs = workLog.getWorklogs();
                if (workLogs != null) {
                    for (WorkLogDto.Log wl : workLogs) {
                        final LocalDate startedLocalDate = DatetimeUtils.dateToLocalDate(wl.getStarted());
                        if (DatetimeUtils.isLocalDateBetween(startedLocalDate, fromDate, toDate)) {
                            totalTimeSpent += wl.getTimeSpentSeconds();
                        }
                    }
                }
            }
        }
        teamResponse.setAvgTimeSpent(NumberUtils.round(totalTimeSpent / (monthCount * members.size() * Constant.TIME_MM)));
    }

    @Nullable
    public IssueSearchResponse getTeamViewIssues(LocalDate fromDate, LocalDate toDate, List<String> worklogAuthors) {
        if (CollectionUtils.isEmpty(worklogAuthors)) {
            return null;
        }
        final String jql = String.format(
                """
                type NOT IN (%s)
                AND worklogAuthor IN (%s)
                AND worklogDate >= "%s"
                AND worklogDate <= "%s"
                """,
                String.join(", ", IGNORE_SEARCH_ISSUE_TYPE),
                String.join(",", worklogAuthors.stream().map(author -> '"' + author + '"').toList()),// Format các worklogAuthors đúng cú pháp
                fromDate,
                toDate
        );

        return jiraApiService.searchIssue(jql, ISSUE_FIELDS);
    }

    public List<JiraProjectDto> getJiraProject(List<String> jiraProjectIds) {
        final var response = jiraApiService.searchProject(jiraProjectIds);
        if (response == null) {
            return new ArrayList<>();
        }
        return response.getValues();
    }

    public List<JiraProjectDto> getJiraProject(String jiraProjectName) {
        final var response = jiraApiService.searchProject(jiraProjectName);
        if (response == null) {
            return new ArrayList<>();
        }
        return List.of(response);
    }
}
