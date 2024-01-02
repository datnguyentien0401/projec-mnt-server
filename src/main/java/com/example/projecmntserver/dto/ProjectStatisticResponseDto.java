package com.example.projecmntserver.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStatisticResponseDto {
    private List<ProjectStatisticDto> tableData;
    private List<ProjectStatisticDto> chartData;

}
