package com.example.projecmntserver.dto;

import com.example.projecmntserver.constant.CommonConstant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProjectStatisticDto {
    private String projectId;
    private String projectName;
    private Long totalStoryPoint = 0L;
    private Long totalTimeSpent = 0L;
    private Integer totalResolvedIssue = 0;
    private Integer totalInProgressIssue = 0;
    private Integer totalOpenIssue = 0;
    private Integer totalHeadCount = 0;
    private String month;

    public Double getTotalTimeSpentMM() {
        return (double) (totalTimeSpent / CommonConstant.TIME_MM);
    }

    public Double getTotalTimeSpentMD() {
        return (double) (totalTimeSpent / CommonConstant.TIME_MD);
    }
}
