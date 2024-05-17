package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.Constant.JIRA_ACCOUNT_TYPE_APP;
import static com.example.projecmntserver.constant.Constant.MAX_RESULT_SEARCH_JIRA;
import static com.example.projecmntserver.constant.JiraParamConstant.FIELDS;
import static com.example.projecmntserver.constant.JiraParamConstant.JQL;
import static com.example.projecmntserver.constant.JiraParamConstant.MAX_RESULTS;
import static com.example.projecmntserver.constant.JiraParamConstant.START_AT;
import com.example.projecmntserver.constant.JiraPathConstant;
import com.example.projecmntserver.dto.jira.IssueSearchResponse;
import com.example.projecmntserver.dto.jira.JiraProjectDto;
import com.example.projecmntserver.dto.jira.UserSearchDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public IssueSearchResponse searchIssue(String jql, String... fields) {
        PageRequest page = PageRequest.ofSize(MAX_RESULT_SEARCH_JIRA);
        String joinedFields = String.join(",", fields);
        final IssueSearchResponse response = searchIssueWithPage(jql, joinedFields, page);
        if (Objects.nonNull(response)) {
            final Integer totalData = response.getTotal();
            while (totalData > page.getPageSize() * (page.getPageNumber() + 1)) {
                page = PageRequest.of(page.getPageNumber() + 1, MAX_RESULT_SEARCH_JIRA);
                final var nextPageResponse = searchIssueWithPage(jql, joinedFields, page);
                if (Objects.isNull(nextPageResponse)) {
                    break;
                }
                response.getIssues().addAll(nextPageResponse.getIssues());
            }
        }
        return response;
    }

    public IssueSearchResponse searchIssueWithPage(String jql, String fields, PageRequest page) {
        final int pageSize = page.getPageSize();
        final StringBuilder urlBuilder = new StringBuilder(jiraBaseUrl + JiraPathConstant.SEARCH);
        urlBuilder.append('?')
                  .append(JQL).append('=')
                  .append(jql)
                  .append('&').append(FIELDS)
                  .append('=')
                  .append(fields)
                  .append('&').append(MAX_RESULTS)
                  .append('=')
                  .append(pageSize)
                  .append('&').append(START_AT)
                  .append('=')
                  .append(page.getPageNumber() * pageSize);
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
