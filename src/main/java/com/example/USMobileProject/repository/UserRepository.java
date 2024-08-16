package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import org.bson.types.ObjectId;

public interface UserRepository extends MongoRepository<UserModel, ObjectId>, UserRepositoryCustom {
    List<ObjectId> findAllBy();
}