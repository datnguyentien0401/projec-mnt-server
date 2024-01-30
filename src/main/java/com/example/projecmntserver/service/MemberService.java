package com.example.projecmntserver.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.projecmntserver.domain.Member;
import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.mapper.MemberMapper;
import com.example.projecmntserver.dto.request.MemberDto;
import com.example.projecmntserver.exception.NotFoundException;
import com.example.projecmntserver.repository.MemberRepository;
import com.example.projecmntserver.repository.TeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final MemberMapper memberMapper;

    public Member create(MemberDto memberDto) {
        final Team team = teamRepository.findById(memberDto.getTeamId()).orElseThrow(
                () -> new NotFoundException("Team not found: " + memberDto.getTeamId()));
        final var memberOptional = memberRepository.findFirstByJiraMemberId(memberDto.getJiraMemberId().trim());
        final Member member;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
            member.setName(memberDto.getName());
        } else {
            member = memberMapper.toEntity(memberDto);
        }
        member.setTeam(team);
        return memberRepository.save(member);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public void delete(Long id) {
        memberRepository.deleteById(id);
    }
}
