package com.example.projecmntserver.dto.jira;

import java.util.List;

import com.example.projecmntserver.dto.jira.IssueDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSearchResponseDto {
    private Integer total;
    private List<IssueDto> issues;

}
