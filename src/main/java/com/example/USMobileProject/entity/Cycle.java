package com.example.USMobileProject.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cycle {
    private String id;
    private String mdn;
    private Date startDate;
    private Date endDate;
    private String userId;
}