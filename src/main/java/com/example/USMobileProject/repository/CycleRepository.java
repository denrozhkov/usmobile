package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.CycleModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.example.USMobileProject.dto.UserIdMdnPair;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

@Repository
public interface CycleRepository extends MongoRepository<CycleModel, ObjectId>, CycleRepositoryCustom {
    
    List<CycleModel> findByMdn(String mdn);

    List<CycleModel> findByUserId(ObjectId userId);
    
    List<CycleModel> findByUserIdAndMdn(ObjectId userId, String mdn);

    List<CycleModel> findByStartDateBetween(Date startDate, Date endDate);
    
    List<CycleModel> findByUserIdAndMdnOrderByStartDateDesc(ObjectId userId, String mdn);
    
    CycleModel findTopByUserIdAndMdnOrderByStartDateDesc(ObjectId userId, String mdn);

    @Query(value = "{ 'userId': ?0, 'mdn': ?1 }", sort = "{ 'startDate' : -1 }")
    Optional<CycleModel> findMaxStartDateByUserIdAndMdn(ObjectId userId, String mdn);
    
    @Aggregation(pipeline = {
        "{ $group: { _id: { userId: '$userId', mdn: '$mdn' } } }",
        "{ $project: { _id: 0, userId: '$_id.userId', mdn: '$_id.mdn' } }"
    })
    List<UserIdMdnPair> findDistinctUserIdsAndMdns();    
}