package com.example.projecmntserver.dto.request;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanningDto {
    private String name;
    @NotBlank
    private String key;
    private LocalDate fromDate;
    private LocalDate toDate;
    @NotNull
    private Object availableWorkingData;
    @NotNull
    private Object requiredWorkforceData;
    @NotNull
    private Object totalWorkforceData;
    @NotNull
    private Object annualLeaveData;
}
