package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.DailyUsageModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Date;
import java.util.List;

public interface DailyUsageRepository extends MongoRepository<DailyUsageModel, ObjectId>, DailyUsageRepositoryCustom {
    @Query("{ 'userId': ?0, 'mdn': ?1, 'usageDate': { $gte: ?2 } }")
    List<DailyUsageModel> findCurrentCycleDailyUsage(ObjectId userId, String mdn, Date startDate);
}