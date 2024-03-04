package com.example.projecmntserver.dto.jira;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.projecmntserver.constant.Constant;
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
    @JsonProperty("summary")
    private String epicName;
    @JsonProperty("customfield_10031")
    private Double storyPoint;
    @JsonProperty("timeestimate")
    private Long timeEstimate;
    @JsonProperty("timespent")
    private Long timeSpent;
    @JsonProperty("duedate")
    @DateTimeFormat(pattern = Constant.DATE_TIME_PATTERN)
    private LocalDate dueDate;
    @JsonProperty("updated")
    private String updatedAt;
}
