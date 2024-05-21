package com.example.projecmntserver.service;

import static com.example.projecmntserver.type.JiraIssueType.IGNORE_SEARCH_ISSUE;
import static com.example.projecmntserver.type.JiraStatus.DONE_STATUS_LIST;

import com.example.projecmntserver.domain.Member;
import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.jira.WorkLogDto;
import com.example.projecmntserver.util.Helper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.projecmntserver.constant.Constant;
import com.example.projecmntserver.dto.jira.EpicDto;
import com.example.projecmntserver.dto.jira.FieldDto;
import com.example.projecmntserver.dto.jira.IssueDto;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.IssueTypeDto;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.jira.ParentDto;
import com.example.projecmntserver.dto.jira.ParentFieldDto;
import com.example.projecmntserver.dto.response.EpicRemainingResponse;
import com.example.projecmntserver.dto.response.OverallTeamResponse;
import com.example.projecmntserver.dto.response.ProjectDto;
import com.example.projecmntserver.dto.response.ProjectResponse;
import com.example.projecmntserver.type.ProjectSearchType;
import com.example.projecmntserver.util.DatetimeUtils;
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
        "worklog"
    };

    private final JiraApiService jiraApiService;

    public List<EpicDto> getAllEpic(List<String> jiraProjectIds, boolean groupEpic, boolean resolvedEpic) {
        return getAllEpics(new ArrayList<>(), jiraProjectIds, groupEpic, resolvedEpic);
    }

    public ProjectResponse getProjectStatisticV2(List<String> epicIds,
                                                 ProjectSearchType type,
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

        getProjectListPerMonthV2(epicIds, type, fromDate, toDate)
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
        return addMonthNoData(projectByMonth, fromDate, toDate);
    }

    private static Map<String, ProjectDto> addMonthNoData(Map<String, ProjectDto> data, LocalDate fromDate,
                                                          LocalDate toDate) {
        final Set<String> monthExists = data.keySet();
        while (!fromDate.isAfter(toDate)) {
            final String monthKey = DatetimeUtils.toMonth(fromDate);
            fromDate = fromDate.plusMonths(1);
            if (monthExists.contains(monthKey)) {
               continue;
            }
            final var value = new ProjectDto();
            value.setMonth(monthKey);
            data.put(monthKey, value);
        }
        return data;
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
                                                                   ProjectSearchType type,
                                                                   LocalDate fromDate,
                                                                   LocalDate toDate) {
        final List<EpicDto> allEpics = getAllEpics(epicIds, new ArrayList<>(), true, true);

        final Map<String, Map<String, ProjectDto>> projectByEpic = new HashMap<>();
        final Map<String, EpicDto> epicMap = new HashMap<>();
        allEpics.forEach(epicDto -> {
            projectByEpic.put(epicDto.getName(), new HashMap<>());
            epicMap.put(epicDto.getName(), epicDto);
        });

        final String jql = String.format("AND 'epic link' IN ( %s )", String.join(", ", epicIds));
        final var response = jiraApiService.searchIssue(
                String.format(" type NOT IN ( %s ) AND updated >= %s AND updated <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql);

        if (Objects.isNull(response)) {
            return new ArrayList<>();
        }

        for (var issue : response.getIssues()) {
            final var fields = issue.getFields();

            final String projectName = getProjectName(fields);
            if (!StringUtils.hasText(projectName)) {
                continue;
            }

            Map<String, ProjectDto> projectByMonth = new HashMap<>();
            if (projectByEpic.containsKey(projectName)) {
                projectByMonth = projectByEpic.get(projectName);
            }

            final LocalDate updatedDate = DatetimeUtils.parseDatetime(fields.getUpdatedAt()).toLocalDate();
            final String month = DatetimeUtils.toMonth(updatedDate);

            var project = new ProjectDto();
            if (projectByMonth.containsKey(month)) {
                project = projectByMonth.get(month);
            } else {
                project.setMonth(month);
                project.setEpicIds(epicMap.get(projectName).getIds());
                project.setEpicName(projectName);
            }

            if ((type == ProjectSearchType.TIME_SPENT_MM || type == ProjectSearchType.TIME_SPENT_MD)
                && Objects.nonNull(fields.getTimeSpent())) {
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
            projectByEpic.put(projectName, projectByMonth);
        }

        if (type == ProjectSearchType.RESOLVED_ISSUE || type == ProjectSearchType.STORY_POINT) {
            getResolvedIssueDataPerMonthV2(projectByEpic, epicMap, type, jql, fromDate, toDate);
        }
        final List<Map<String, ProjectDto>> values = new ArrayList<>(projectByEpic.values());
        values.forEach(v -> addMonthNoData(v, fromDate, toDate));
        return values;
    }

    private static String getProjectName(FieldDto fields) {
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
            return getEpicPrefix(parentFields.getSummary());
        }
        return Constant.EMPTY_STRING;
    }

    private void getResolvedIssueDataPerMonthV2(Map<String, Map<String, ProjectDto>> projectByEpic,
                                                Map<String, EpicDto> epicMap, ProjectSearchType type,
                                                String jql, LocalDate fromDate, LocalDate toDate) {
        final var response = jiraApiService.searchIssue(
                String.format("type NOT IN ( %s ) AND resolved >= %s AND resolved <= %s ",
                              String.join(", ", IGNORE_SEARCH_ISSUE), fromDate, toDate) + jql);
        if (Objects.nonNull(response)) {
            final var issues = response.getIssues();
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

                if (type == ProjectSearchType.RESOLVED_ISSUE) {
                    project.setTotalResolvedIssue(project.getTotalResolvedIssue() + 1);
                } else if (Objects.nonNull(fields.getStoryPoint())) {
                    project.setTotalStoryPoint(
                            project.getTotalStoryPoint() + fields.getStoryPoint());
                }

                projectByMonth.put(month, project);
                projectByEpic.put(projectName, projectByMonth);
            }
        }
    }

    public List<EpicDto> getAllEpics(List<String> epicIds, List<String> jiraProjectIds, boolean groupEpic, boolean resolvedEpic) {
        String jql = "issuetype = 'epic' ";
        if (!CollectionUtils.isEmpty(epicIds)) {
            jql += String.format(" AND id IN (%s) ", String.join(",", epicIds));
        } else if (!CollectionUtils.isEmpty(jiraProjectIds)) {
            jql += String.format(" AND project IN (%s) ", String.join(",", jiraProjectIds));
        }
        if (!resolvedEpic) {
            jql += String.format(" AND status NOT IN (%s) ",
                                 String.join(",", DONE_STATUS_LIST));
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
                epicDto.setKey(epic.getKey());
                epicDto.setDueDate(epic.getFields().getDueDate());
                epicDto.setStatus(epic.getFields().getStatus().getName());
                epicGroups.put(epicName, epicDto);
            }
            allEpics.addAll(epicGroups.values());
        }
        return allEpics;
    }

    private static String getEpicPrefix(String epicName) {
        return StringUtils.hasText(epicName) ? epicName.split("_")[0] : Constant.EMPTY_STRING;
    }

    public List<EpicRemainingResponse> getEpicRemaining(List<String> epicIds) {
        final List<EpicRemainingResponse> result = new ArrayList<>();
        final List<EpicDto> allEpics = getAllEpics(epicIds, new ArrayList<>(), false, false);
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

    public List<OverallTeamResponse> getOverall(LocalDate fromDate, LocalDate toDate, List<Team> allTeams,
        Map<Long, @NotNull List<Member>> teamMembers) {
        final List<OverallTeamResponse> result = new ArrayList<>();

        final var jiraMemberIds = teamMembers.values().stream().flatMap(List::stream).map(Member::getJiraMemberId)
            .distinct().toList();
        IssueSearchResponse issueSearchResponse = getTeamViewIssues(fromDate, toDate, jiraMemberIds);

        final var issuesByAssignee = issueSearchResponse.getIssues().stream()
            .collect(Collectors.groupingBy(issueDto -> issueDto.getFields().getAssignee().getAccountId()));

        for (var team : allTeams) {
            final var members = teamMembers.get(team.getId());
            if (members.isEmpty()) {
                continue;
            }
            List<IssueDto> issues = members.stream().map(member -> issuesByAssignee.get(member.getJiraMemberId()))
                .filter(Objects::nonNull)
                .flatMap(List::stream).toList();
            OverallTeamResponse teamResponse = new OverallTeamResponse();
            teamResponse.setTeam(team.getName());
            setTeamOverallResolvedIssueAndStoryPointData(teamResponse, issues, members, fromDate, toDate);
            setTeamOverallTimeSpent(teamResponse, issues, members, fromDate, toDate);
            result.add(teamResponse);
        }

        return result;
    }

    private void setTeamOverallResolvedIssueAndStoryPointData(OverallTeamResponse teamResponse,
        List<IssueDto> issues, List<Member> members, LocalDate fromDate, LocalDate toDate) {
        List<IssueDto> resolvedIssues = Helper.getResolvedIssuesInRange(issues, fromDate, toDate);
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

    private void setTeamOverallTimeSpent(OverallTeamResponse teamResponse,
        List<IssueDto> issues, List<Member> members, LocalDate fromDate, LocalDate toDate) {
        double totalTimeSpent = 0.0;
        final long monthCount = DatetimeUtils.countMonthConsideringToday(fromDate, toDate);
        for (IssueDto issue : issues) {
            final var fields = issue.getFields();
            WorkLogDto workLog = fields.getWorklog();
            if (workLog != null) {
                List<WorkLogDto.Log> workLogs = workLog.getWorklogs();
                if (workLogs != null) {
                    for (WorkLogDto.Log wl : workLogs) {
                        Date started = wl.getStarted();
                        LocalDate startedLocalDate = DatetimeUtils.dateToLocalDate(started);
                        if (DatetimeUtils.isLocalDateBetween(startedLocalDate, fromDate, toDate)) {
                            totalTimeSpent += wl.getTimeSpentSeconds();
                        }
                    }
                }
            }
        }
        teamResponse.setAvgTimeSpent(NumberUtils.round(totalTimeSpent / (monthCount * members.size() * Constant.TIME_MD)));
    }

    public IssueSearchResponse getTeamViewIssues(LocalDate fromDate, LocalDate toDate, List<String> jiraMemberIds) {
        String jql = String.format(
            """
                type NOT IN (%s)
                AND (
                        (resolved >= %s AND resolved <= %s)
                    OR  (created <= %s AND updated >= %s)
                ) \
            """,
            String.join(", ", IGNORE_SEARCH_ISSUE),
            fromDate,
            toDate,
            toDate,
            fromDate);
        if (!CollectionUtils.isEmpty(jiraMemberIds)) {
            jql += String.format("AND assignee IN ( %s )", String.join(",", jiraMemberIds));
        }
        return jiraApiService.searchIssue(jql, ISSUE_FIELDS);
    }

    public List<JiraProjectDto> getJiraProject(String jiraProjectName) {
        final var response = jiraApiService.getAllProject(jiraProjectName);
        if (response == null) {
            return new ArrayList<>();
        }
        return List.of(response);
    }
}
