package com.example.USMobileProject.repository;

import com.mongodb.bulk.BulkWriteResult;
import org.bson.Document;
import com.mongodb.client.model.WriteModel;
import org.bson.types.ObjectId;
import java.util.List;

public interface UserRepositoryCustom {
    BulkWriteResult bulkUserInsert(List<WriteModel<Document>> documents);
}