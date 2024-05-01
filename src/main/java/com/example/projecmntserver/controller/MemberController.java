package com.example.projecmntserver.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projecmntserver.dto.mapper.MemberMapper;
import com.example.projecmntserver.dto.request.MemberDto;
import com.example.projecmntserver.dto.response.MemberResponse;
import com.example.projecmntserver.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@Validated
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<List<MemberResponse>> getAllByTeamId(@PathVariable Long teamId) {
        return ResponseEntity.ok(memberMapper.toResponse(memberService.findByTeamId(teamId)));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAll() {
        return ResponseEntity.ok(memberMapper.toResponse(memberService.findAll()));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberDto memberDto) {
        return ResponseEntity.ok(memberMapper.toResponse(memberService.create(memberDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
