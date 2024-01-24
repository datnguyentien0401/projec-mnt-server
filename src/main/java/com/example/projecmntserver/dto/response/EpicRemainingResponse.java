package com.example.projecmntserver.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.example.projecmntserver.constant.Constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpicRemainingResponse {
    private String epicName;
    private List<String> epicIds;
    private LocalDate expectedDate;
    private LocalDate dueDate;
    private Long timeEstimate = 0L;
    private String status;
    private Integer headCount = 0;

    public Double getTimeEstimateMM() {
        return (double) timeEstimate / Constant.TIME_MM;
    }
}
