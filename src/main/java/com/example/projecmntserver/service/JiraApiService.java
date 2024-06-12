package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.Constant.JIRA_ACCOUNT_TYPE_APP;
import static com.example.projecmntserver.constant.Constant.MAX_RESULT_SEARCH_JIRA;
import static com.example.projecmntserver.constant.JiraParamConstant.EXPAND;
import static com.example.projecmntserver.constant.JiraParamConstant.FIELDS;
import static com.example.projecmntserver.constant.JiraParamConstant.JQL;
import static com.example.projecmntserver.constant.JiraParamConstant.MAX_RESULTS;
import static com.example.projecmntserver.constant.JiraParamConstant.START_AT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.projecmntserver.constant.JiraPathConstant;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.jira.JiraProjectSearchResponse;
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

    public JiraProjectDto[] searchProject(String projectName) {
        return restTemplate.exchange(new StringBuilder(jiraBaseUrl).append(JiraPathConstant.GET_ALL)
                                                                   .append(String.format("?query=%s", projectName))
                                                                   .toString(),
                                     HttpMethod.GET, httpEntity, JiraProjectDto[].class).getBody();
    }

    public JiraProjectSearchResponse searchProject(List<String> projectIds) {
        final StringBuilder url = new StringBuilder(jiraBaseUrl).append(JiraPathConstant.PROJECT_SEARCH).append("?1=1");
        projectIds.forEach(id -> url.append(String.format("&id=%s", id)));

        return restTemplate.exchange(url.toString(), HttpMethod.GET, httpEntity,
                                     JiraProjectSearchResponse.class).getBody();
    }

    public IssueSearchResponse searchIssueExpand(String jql, String expand, String... fields) {
        final String joinedFields = String.join(",", fields);
        final Map<String, String> params = Map.of(JQL, jql,
                                                  EXPAND, expand,
                                                  FIELDS, joinedFields);
        return searchIssue(params);
    }

    public IssueSearchResponse searchIssue(String jql, String... fields) {
        final String joinedFields = String.join(",", fields);
        final Map<String, String> params = Map.of(JQL, jql, FIELDS, joinedFields);
        return searchIssue(params);
    }

    public IssueSearchResponse searchIssue(Map<String, String> params) {
        PageRequest page = PageRequest.ofSize(MAX_RESULT_SEARCH_JIRA);

        final IssueSearchResponse response = searchIssueWithPage(params, page);
        if (Objects.nonNull(response)) {
            final Integer totalData = response.getTotal();
            while (totalData > page.getPageSize() * (page.getPageNumber() + 1)) {
                page = PageRequest.of(page.getPageNumber() + 1, MAX_RESULT_SEARCH_JIRA);
                final var nextPageResponse = searchIssueWithPage(params, page);
                if (Objects.isNull(nextPageResponse)) {
                    break;
                }
                response.getIssues().addAll(nextPageResponse.getIssues());
            }
        }
        return response;
    }

    public IssueSearchResponse searchIssueWithPage(Map<String, String> params, PageRequest page) {
        final int pageSize = page.getPageSize();
        final StringBuilder urlBuilder = new StringBuilder(jiraBaseUrl + JiraPathConstant.SEARCH);
        urlBuilder.append('?')
                  .append(MAX_RESULTS)
                  .append('=')
                  .append(pageSize)
                  .append('&').append(START_AT)
                  .append('=')
                  .append(page.getPageNumber() * pageSize);
        params.forEach((paramName, value) -> urlBuilder.append('&').append(paramName).append('=').append(value));

        final var res = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, httpEntity,
                                              IssueSearchResponse.class);
        if (res.getStatusCode().is2xxSuccessful()) {
            return res.getBody();
        }
        return new IssueSearchResponse();
    }

    public List<UserSearchDto> searchUser(String username) {
        final String url = jiraBaseUrl + JiraPathConstant.USER_SEARCH + String.format("?query=%s&maxResults=%s", username, MAX_RESULT_SEARCH_JIRA);
        final ResponseEntity<UserSearchDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                                                                               UserSearchDto[].class);
        final UserSearchDto[] users = response.getBody();
        if (Objects.nonNull(users) && users.length > 0) {
            return Arrays.stream(users).filter(u -> !JIRA_ACCOUNT_TYPE_APP.equals(u.getAccountType())).toList();
        }
        return new ArrayList<>();
    }
}
