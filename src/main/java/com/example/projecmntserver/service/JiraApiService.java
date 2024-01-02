package com.example.projecmntserver.service;

import static com.example.projecmntserver.constant.JiraParamConstant.FIELDS;
import static com.example.projecmntserver.constant.JiraParamConstant.JQL;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.projecmntserver.constant.JiraPathConstant;
import com.example.projecmntserver.dto.jira.ProjectDto;
import com.example.projecmntserver.dto.jira.ProjectSearchResponseDto;

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

    public ProjectSearchResponseDto searchProject(String jql, String fields) {
        final StringBuilder urlBuilder = new StringBuilder(jiraBaseUrl + JiraPathConstant.SEARCH);
        urlBuilder.append('?')
                  .append(JQL).append('=')
                  .append(jql)
                  .append('&').append(FIELDS)
                  .append('=')
                  .append(fields);
        final var res = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, httpEntity,
                                              ProjectSearchResponseDto.class);
        log.info("search project error: {}", res.getStatusCode());
        if (res.getStatusCode().is2xxSuccessful()) {
            return res.getBody();
        }
        return new ProjectSearchResponseDto();
    }
}
