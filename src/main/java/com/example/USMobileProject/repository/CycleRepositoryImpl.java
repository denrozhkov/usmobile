package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.CycleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import com.mongodb.bulk.BulkWriteResult;
import org.springframework.stereotype.Repository;
import org.bson.types.ObjectId;
import java.util.List;

@Repository
public class CycleRepositoryImpl implements CycleRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public CycleRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public int saveBatch(List<CycleModel> cycleBatch) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkMode.UNORDERED, CycleModel.class);
        bulkOps.insert(cycleBatch);
        BulkWriteResult result = bulkOps.execute();
        return result.getInsertedCount();
    }

}