package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.JiraParamConstant.FIELDS;
import static com.example.projecmntserver.constant.JiraParamConstant.JQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.projecmntserver.constant.JiraPathConstant;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.jira.UserSearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JiraApiService {
    @Value("${jira.base-url}")
    private String jiraBaseUrl;
    private final RestTemplate restTemplate;
    private final HttpEntity<String> httpEntity;

    public JiraProjectDto[] getAllProject(String projectName) {
        return restTemplate.exchange(new StringBuilder(jiraBaseUrl).append(JiraPathConstant.GET_ALL)
                                                                   .append(String.format("?query=%s", projectName))
                                                                   .toString(),
                                     HttpMethod.GET, httpEntity, JiraProjectDto[].class).getBody();
    }

    public IssueSearchResponse searchIssue(String jql) {
        return searchIssue(jql, "");
    }

    public IssueSearchResponse searchIssue(String jql, String fields) {
        final StringBuilder urlBuilder = new StringBuilder(jiraBaseUrl + JiraPathConstant.SEARCH);
        urlBuilder.append('?')
                  .append(JQL).append('=')
                  .append(jql)
                  .append('&').append(FIELDS)
                  .append('=')
                  .append(fields);
        final var res = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, httpEntity,
                                              IssueSearchResponse.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            return res.getBody();
        }
        return new IssueSearchResponse();
    }

    public List<UserSearchDto> searchUser(String username) {
        final String url = jiraBaseUrl + JiraPathConstant.USER_SEARCH + String.format("?query=%s", username);
        final ResponseEntity<UserSearchDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                                                                               UserSearchDto[].class);
        final UserSearchDto[] users = response.getBody();
        if (Objects.nonNull(users) && users.length > 0) {
            return Arrays.stream(users).filter(u -> !"app".equals(u.getAccountType())).toList();
        }
        return new ArrayList<>();
    }
}
