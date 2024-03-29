package com.example.projecmntserver.dto.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraProjectDto {
    private String id;
    private String key;
    private String name;
}
