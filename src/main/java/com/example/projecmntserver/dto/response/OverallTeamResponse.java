package com.example.projecmntserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverallTeamResponse {
    private String team;
    private Integer totalResolvedIssue;
    private Double avgResolvedIssue;
    private Double avgStoryPoint;
    private Double avgTimeSpent;
}
