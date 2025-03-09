package com.example.projecmntserver.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.projecmntserver.constant.Constant;
import com.example.projecmntserver.domain.Member;
import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.jira.IssueDto;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.WorkLogDto;
import com.example.projecmntserver.dto.mapper.TeamMapper;
import com.example.projecmntserver.dto.request.TeamDto;
import com.example.projecmntserver.dto.response.OverallTeamResponse;
import com.example.projecmntserver.dto.response.TeamResponse;
import com.example.projecmntserver.dto.response.TeamViewResponse;
import com.example.projecmntserver.repository.MemberRepository;
import com.example.projecmntserver.repository.TeamRepository;
import com.example.projecmntserver.util.DatetimeUtils;
import com.example.projecmntserver.util.Helper;
import com.example.projecmntserver.util.NumberUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMapper teamMapper;
    private final ProjectService projectService;

    public Team create(TeamDto teamDto) {
        final Team team = teamRepository.findByName(teamDto.getName().trim())
                                        .orElse(teamMapper.toEntity(teamDto));
        return teamRepository.save(team);
    }

    public List<TeamResponse> findAll() {
        return teamRepository.findAllWithNumberOfMembers();
    }

    @Transactional
    public void delete(Long id) {
        memberRepository.deleteAllByTeamId(id);
        teamRepository.deleteById(id);
    }

    public List<OverallTeamResponse> getOverall(LocalDate fromDate, LocalDate toDate) {
        final List<Team> allTeams = teamRepository.findAll();
        final Map<Long, List<Member>> teamMembers = new HashMap<>();
        for (var team : allTeams) {
            final var members = memberRepository.findMembersByTeamId(team.getId());
            teamMembers.put(team.getId(), members);
        }
        return projectService.getOverall(fromDate, toDate, allTeams, teamMembers);
    }

    public TeamViewResponse getTeamView(LocalDate fromDate, LocalDate toDate, Long teamId) {
        final TeamViewResponse result = new TeamViewResponse();
        final List<Member> members = memberRepository.findMembersByTeamId(teamId);

        final Map<String, String> memberByJiraId =
                members.stream().collect(Collectors.toMap(Member::getJiraMemberId, Member::getName));

        final var teamViewIssues = projectService.getTeamViewIssues(fromDate, toDate,
            new ArrayList<>(memberByJiraId.keySet()));

        setResolvedIssueAndStoryPointData(result, fromDate, toDate, memberByJiraId, teamViewIssues);
        setTimeSpentData(result, fromDate, toDate, teamViewIssues);

        return result;
    }

    private static void setResolvedIssueAndStoryPointData(TeamViewResponse result,
                                                          LocalDate fromDate, LocalDate toDate,
                                                          Map<String, String> member,
                                                          IssueSearchResponse teamViewIssues) {
        final Map<Integer, Map<String, Object>> resolvedIssueData = new HashMap<>();
        final Map<String, Map<Integer, Integer>> resolvedIssueChartData = new HashMap<>();
        final Map<Integer, Map<String, Object>> storyPointData = new HashMap<>();

        if(Objects.nonNull(teamViewIssues)) {
            final var issues = teamViewIssues.getIssues();
            Helper.getResolvedIssuesInRange(issues, fromDate, toDate).forEach(issue -> {
                final var fields = issue.getFields();
                final var assignee = fields.getAssignee();
                if (Objects.isNull(assignee)) {
                    return;
                }
                final String accountId = assignee.getAccountId();
                final int month = DatetimeUtils.parseDatetime(fields.getResolvedAt(), null).getMonthValue();
                setData(resolvedIssueData, month, accountId, 1);
                setChartData(resolvedIssueChartData, accountId, month, 1);
                if (Objects.nonNull(fields.getStoryPoint())) {
                    setData(storyPointData, month, assignee.getAccountId(), fields.getStoryPoint());
                }
            });
        }
        result.setResolvedIssueData(addMonthHasNoDataAndRoundData(resolvedIssueData, fromDate, toDate));
        result.setStoryPointData(addMonthHasNoDataAndRoundData(storyPointData, fromDate, toDate));

        resolvedIssueChartData.forEach((chartDataKey, map) -> {
            final Map<String, Object> resolvedIssueChartDataFormatted = new HashMap<>();
            resolvedIssueChartDataFormatted.put("name", member.get(chartDataKey));
            map.forEach((key, value) -> resolvedIssueChartDataFormatted.put(DatetimeUtils.toMonth(key, null), value));
            result.getResolvedIssueChartData().add(resolvedIssueChartDataFormatted);
        });
    }

    private static void setTimeSpentData(TeamViewResponse result, LocalDate fromDate, LocalDate toDate,
        IssueSearchResponse teamViewIssues) {
        final Map<Integer, Map<String, Object>> timeSpentData = new HashMap<>();

        if (Objects.nonNull(teamViewIssues)) {
            final var issues = teamViewIssues.getIssues();
            for (var issue : issues) {
                setTimeSpentData(fromDate, toDate, issue, timeSpentData);
            }
        }
        result.setTimeSpentData(addMonthHasNoDataAndRoundData(timeSpentData, fromDate, toDate));
    }

    private static void setTimeSpentData(LocalDate fromDate, LocalDate toDate, IssueDto issue,
        Map<Integer, Map<String, Object>> timeSpentData) {
        final var fields = issue.getFields();
        final var assignee = fields.getAssignee();
        if (Objects.isNull(assignee)) {
            return;
        }
        final WorkLogDto worklog = fields.getWorklog();
        if (Objects.nonNull(worklog)) {
            final List<WorkLogDto.Log> workLogs = worklog.getWorklogs();
            if (!CollectionUtils.isEmpty(workLogs)) {
                workLogs.forEach(wl -> {
                    final String accountId = wl.getAuthor().getAccountId();
                    final Date started = wl.getStarted();

                    final ZonedDateTime startedZoned = ZonedDateTime.ofInstant(started.toInstant(), ZoneId.of("UTC+7"));
                    final LocalDate startedLocalDate = startedZoned.toLocalDate();

                    if (DatetimeUtils.isLocalDateBetween(startedLocalDate, fromDate, toDate)) {
                        setData(timeSpentData, startedZoned.getMonthValue(), accountId,
                            (double) wl.getTimeSpentSeconds() / Constant.TIME_MD);
                    }
                });
            }
        }
    }

    private static List<Map<String, Object>> addMonthHasNoDataAndRoundData(Map<Integer, Map<String, Object>> dataByMonth,
                                          LocalDate fromDate, LocalDate toDate) {
        final Set<Integer> monthHasData = dataByMonth.keySet();
        final int fromMonth = fromDate.getMonthValue();
        final int toMonth = toDate.getMonthValue();

        for (int month = fromMonth; month <= toMonth; month++) {
            if (monthHasData.contains(month)) {
                final var data = dataByMonth.get(month);
                data.forEach((k, v) -> {
                    if (v instanceof Double) {
                        data.put(k, NumberUtils.round((double) v));
                    }
                });
                data.put("month", (double) month);
                dataByMonth.put(month, data);
            } else {
                dataByMonth.put(month, Map.of("month", (double) month));
            }
        }
        return new ArrayList<>(dataByMonth.values());
    }

    private static <T> void setData(Map<Integer, Map<String, T>> data, int dataKey,
                                    String mapKey, T value) {
        Map<String, T> map = new HashMap<>();
        if (data.containsKey(dataKey)) {
            map = data.get(dataKey);
            if (map.containsKey(mapKey)) {
                map.put(mapKey, (T) NumberUtils.add((Number) map.get(mapKey), (Number) value));
            } else {
                map.put(mapKey, value);
            }
            data.put(dataKey, map);
        } else {
            map.put(mapKey, value);
            data.put(dataKey, map);
        }
    }

    private static <T> void setChartData(Map<String, Map<Integer, T>> data, String chartDataKey,
                                         int mapKey,T value) {
        Map<Integer, T> map = new HashMap<>();
        if (data.containsKey(chartDataKey)) {
            map = data.get(chartDataKey);
            if (map.containsKey(mapKey)) {
                map.put(mapKey, (T) NumberUtils.add((Number) map.get(mapKey), (Number) value));
            } else {
                map.put(mapKey, value);
            }
            data.put(chartDataKey, map);
        } else {
            map.put(mapKey, value);
            data.put(chartDataKey, map);
        }
    }
}
