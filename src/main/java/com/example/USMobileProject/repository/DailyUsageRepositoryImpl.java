package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.DailyUsageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.BulkOperations;

import java.util.List;

public class DailyUsageRepositoryImpl implements DailyUsageRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public int bulkInsertDailyUsage(List<DailyUsageModel> dailyUsages) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, DailyUsageModel.class);
        bulkOps.insert(dailyUsages);
        return bulkOps.execute().getInsertedCount();
    }
}