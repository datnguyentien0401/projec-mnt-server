package com.example.projecmntserver.dto.response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.example.projecmntserver.constant.Constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProjectDto {
    private List<String> epicIds = new ArrayList<>();
    private String epicName;
    private Double totalStoryPoint = 0.0;
    private Long totalTimeSpent = 0L;
    private Integer totalResolvedIssue = 0;
    private Integer totalInProgressIssue = 0;
    private Integer totalOpenIssue = 0;
    private Integer totalHeadCount = 0;
    private String month;
    private boolean forColumnChart;
    private Set<String> assignees = new HashSet<>();

    public String getProjectName() {
        return epicName;
    }

    public String getProjectId() {
        return CollectionUtils.isEmpty(epicIds) ? "" : String.join("-", epicIds);
    }

    public Double getTotalTimeSpentMM() {
        return (double) totalTimeSpent / Constant.TIME_MM;
    }

    public Double getTotalTimeSpentMD() {
        return (double) totalTimeSpent / Constant.TIME_MD;
    }

    public Integer getTotalHeadCount() {
        return assignees.size();
    }
}
