package com.example.USMobileProject.dto;

import lombok.Data;
import java.util.Date;

@Data
public class DailyUsageDTO {
    private Date date;
    private double dailyUsage;
}