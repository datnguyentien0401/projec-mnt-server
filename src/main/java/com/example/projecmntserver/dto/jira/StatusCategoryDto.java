package com.example.projecmntserver.dto.jira;

import com.example.projecmntserver.type.StatusCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusCategoryDto {
    private String id;
    private StatusCategory key;
    private String name;
}
