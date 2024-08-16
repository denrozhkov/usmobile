package com.example.USMobileProject.repository;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.WriteModel;
import org.springframework.stereotype.Repository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.*;
import org.bson.types.ObjectId;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private static final String USERS_COLLECTION_NAME = "Users";

    public BulkWriteResult bulkUserInsert(List<WriteModel<Document>> documents) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(USERS_COLLECTION_NAME);
        return collection.bulkWrite(documents);
    }
}