package com.example.projecmntserver.dto.jira;

import com.example.projecmntserver.constant.Constant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class FieldDto {
    private JiraProjectDto project;
    private AssigneeDto assignee;
    private StatusDto status;
    @JsonProperty("summary")
    private String epicName;
    @JsonProperty("customfield_10308")
    private Double storyPoint;
    @JsonProperty("timeestimate")
    private Long timeEstimate;
    @JsonProperty("timespent")
    private Long timeSpent;
    @JsonProperty("duedate")
    @DateTimeFormat(pattern = Constant.DATE_TIME_PATTERN)
    private LocalDate dueDate;
    private Date created;
    @JsonProperty("updated")
    private String updatedAt;
    @JsonProperty("resolutiondate")
    private String resolvedAt;
    private ParentDto parent;
    private WorkLogDto worklog;
}
