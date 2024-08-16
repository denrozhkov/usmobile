package com.example.USMobileProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleHistoryDTO {
    private String cycleId;
    private Date startDate;
    private Date endDate;
}