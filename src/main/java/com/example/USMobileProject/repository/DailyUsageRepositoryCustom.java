package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.DailyUsageModel;
import java.util.List;

public interface DailyUsageRepositoryCustom {
    int bulkInsertDailyUsage(List<DailyUsageModel> dailyUsages);
}