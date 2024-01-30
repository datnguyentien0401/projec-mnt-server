package com.example.projecmntserver.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.mapper.TeamMapper;
import com.example.projecmntserver.dto.request.TeamDto;
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

    public Team create(TeamDto teamDto) {
        final Team team = teamRepository.findByName(teamDto.getName())
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
}
