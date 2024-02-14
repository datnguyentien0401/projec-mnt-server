package com.example.projecmntserver.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.projecmntserver.domain.Member;
import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.mapper.TeamMapper;
import com.example.projecmntserver.dto.request.TeamDto;
import com.example.projecmntserver.dto.response.OverallTeamResponse;
import com.example.projecmntserver.repository.MemberRepository;
import com.example.projecmntserver.repository.TeamRepository;

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

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        memberRepository.deleteAllByTeamId(id);
        teamRepository.deleteById(id);
    }

    public List<OverallTeamResponse> getOverall(LocalDate fromDate, LocalDate toDate) {
        final List<OverallTeamResponse> result = new ArrayList<>();
        final List<Team> allTeams = teamRepository.findAll();
        for (var team : allTeams) {
            final List<Member> teamMembers = memberRepository.findMembersByTeamId(team.getId());
            final List<String> jiraMemberIds = teamMembers.stream()
                                                          .map(Member::getJiraMemberId)
                                                          .toList();
            if (CollectionUtils.isEmpty(jiraMemberIds)) {
                continue;
            }
            final var teamOverall = projectService.getTeamOverall(fromDate, toDate, jiraMemberIds);
            teamOverall.setTeam(team.getName());
            result.add(teamOverall);
        }
        return result;
    }
}
