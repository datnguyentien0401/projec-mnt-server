package com.example.projecmntserver.dto.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class FieldDto {
    private ProjectDto project;
    private AssigneeDto assignee;
    private StatusDto status;

    @JsonProperty("timeestimate")
    private Long timeEstimate;
    @JsonProperty("timespent")
    private Long timeSpent;
}
