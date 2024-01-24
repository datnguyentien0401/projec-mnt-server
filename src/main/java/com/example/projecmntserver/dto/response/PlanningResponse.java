package com.example.projecmntserver.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class PlanningResponse extends BaseResponse {
    private String name;
    private String key;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Object availableWorkingData;
    private Object requiredWorkforceData;
    private Object totalWorkforceData;
    private Object annualLeaveData;
}
