package com.example.projecmntserver.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plannings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Planning extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "table_key")
    private String tableKey;
    @Column(name = "from_date")
    private LocalDate fromDate;
    @Column(name = "to_date")
    private LocalDate toDate;
    @Column(name = "available_working_data")
    private String availableWorkingData;
    @Column(name = "required_workforce_data")
    private String requiredWorkforceData;
    @Column(name = "total_workforce_data")
    private String totalWorkforceData;
    @Column(name = "annual_leave_data")
    private String annualLeaveData;

}
