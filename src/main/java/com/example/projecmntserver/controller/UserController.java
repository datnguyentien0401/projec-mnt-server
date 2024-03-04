package com.example.projecmntserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.projecmntserver.dto.jira.UserSearchDto;
import com.example.projecmntserver.service.JiraApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final JiraApiService jiraApiService;
    @GetMapping("/search")
    public ResponseEntity<UserSearchDto[]> search(@RequestParam String username) {
        return jiraApiService.searchUser(username);
    }
}
