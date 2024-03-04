package com.example.projecmntserver.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamViewResponse {
    List<Map<String, Long>> timeSpentData = new ArrayList<>();
    List<Map<String, Double>> storyPointData = new ArrayList<>();
    List<Map<String, Integer>> resolvedIssueData = new ArrayList<>();
    List<Map<String, Object>> resolvedIssueChartData = new ArrayList<>();
}
