package com.example.projecmntserver.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProjectStatisticByProjectIdDto {
    private String projectId;
    private List<ProjectStatisticDto> data;
}
