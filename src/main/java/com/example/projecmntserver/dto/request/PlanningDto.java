package com.example.projecmntserver.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanningDto {
    private String name;
    private String key;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Object availableWorkingData;
    private Object requiredWorkforceData;
    private Object totalWorkforceData;
    private Object annualLeaveData;
}
