package com.example.projecmntserver.dto.jira;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpicDto {
    private List<String> ids = new ArrayList<>();
    private String name;
    private String key;
    private LocalDate dueDate;
    private String status;
    public String getProjectName() {
        return String.format("[%s] %s", key, name);
    }

    public String getProjectId() {
        return String.join("-", ids);
    }
}
