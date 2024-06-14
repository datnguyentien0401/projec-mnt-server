package com.example.projecmntserver.dto.jira;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraProjectSearchResponse {
    private List<JiraProjectDto> values;
}
