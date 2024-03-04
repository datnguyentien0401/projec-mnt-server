package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.JiraParamConstant.FIELDS;
import static com.example.projecmntserver.constant.JiraParamConstant.JQL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.projecmntserver.constant.JiraPathConstant;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.ProjectDto;
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

    public ProjectDto[] getAllProject() {
        return restTemplate.exchange(jiraBaseUrl + JiraPathConstant.GET_ALL, HttpMethod.GET, httpEntity,
                                     ProjectDto[].class).getBody();
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

    public ResponseEntity<UserSearchDto[]> searchUser(String username) {
        final String url = jiraBaseUrl + JiraPathConstant.USER_SEARCH + String.format("?query=%s", username);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, UserSearchDto[].class);
    }
}
