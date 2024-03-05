package com.example.projecmntserver.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private List<ProjectDto> totalData = new ArrayList<>();
    private List<ProjectDto> listData = new ArrayList<>();

}
