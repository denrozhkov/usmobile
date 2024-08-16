package com.example.USMobileProject.service;

import com.github.javafaker.Faker;
import com.mongodb.bulk.BulkWriteResult;
import org.bson.types.ObjectId;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import com.example.USMobileProject.repository.UserRepository;
import com.example.USMobileProject.entity.User;
import com.example.USMobileProject.model.UserModel;
import com.example.USMobileProject.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import com.example.USMobileProject.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Value("${app.max-users-number:10000}")
    private int maxUsersNumber;

    @Value("${app.batch-size:1000}")
    private int batchSize;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserMapper userMapper;

    @Transactional
    public User createUser(User user) {
        logger.info("Creating new user");
        if (user == null) {
            logger.error("Attempt to create null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            validateUserData(user, true);
            // TODO: Check if user with the same email already exists

            UserModel userModel = userMapper.toModel(user);
            UserModel savedUserModel = userRepository.save(userModel);
            logger.info("User created successfully with id: {}", savedUserModel.getId());
            return userMapper.toEntityWithoutPassword(savedUserModel);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error occurred while creating user: {}", e.getMessage());
            throw new ServiceException("Failed to create user", e);
        }
    }
    
    @Transactional
    public int generateUsers(int numberOfUsers) {
        if (numberOfUsers > maxUsersNumber) {
            logger.error("Requested number of users {} exceeds maximum allowed {}", numberOfUsers, maxUsersNumber);
            throw new IllegalArgumentException("Number of users exceeds maximum allowed");
        }

        Faker faker = new Faker();
        int insertedCount = 0;

        try {
            for (int i = 0; i < numberOfUsers; i += batchSize) {
                List<WriteModel<Document>> documents = new ArrayList<>(batchSize);
                for (int j = 0; j < batchSize && (i + j) < numberOfUsers; j++) {
                    Document doc = new Document()
                        .append("firstName", faker.name().firstName())
                        .append("lastName", faker.name().lastName())
                        .append("email", faker.internet().emailAddress())
                        .append("password", faker.internet().password(8, 12));
                    documents.add(new InsertOneModel<>(doc));
                }

                BulkWriteResult result = userRepository.bulkUserInsert(documents);
                insertedCount += result.getInsertedCount();
                logger.info("Inserted {} documents. Total: {}", result.getInsertedCount(), insertedCount);
            }
        } catch (Exception e) {
            logger.error("Error during bulk insert: {}", e.getMessage());
            throw new RuntimeException("Failed to complete bulk insert", e);
        }

        return insertedCount;
    }
    
    @Transactional
    public User updateUser(String idString, User user) {
        logger.info("Updating user with id: {}", idString);
        
        if (idString == null || idString.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (user == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be updated through this method");
        }
        
        ObjectId id = new ObjectId(idString);

        try {
            UserModel existingUserModel = userRepository.findById(id)
                                         .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

            validateUserData(user);

            userMapper.updateUserModel(existingUserModel, user);

            UserModel updatedUserModel = userRepository.save(existingUserModel);
            logger.info("User updated successfully: {}", updatedUserModel);

            return userMapper.toEntityWithoutPassword(updatedUserModel);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating user with id {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating user with id {}: {}", id, e.getMessage(), e);
            throw new ServiceException("Failed to update user", e);
        }
    }
    
    public User getUserById(String idString) {
        ObjectId id;
        try {
            id = new ObjectId(idString);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user ID format: {}", idString);
            throw new IllegalArgumentException("Invalid user ID format", e);
        }
    
        try {
            return userRepository.findById(id)
                    .map(userMapper::toEntityWithoutPassword)
                    .orElseThrow(() -> {
                        logger.warn("User not found with id: {}", id);
                        return new ResourceNotFoundException("User not found with id: " + id);
                    });
        } catch (ResourceNotFoundException e) {
            throw e;                    
        } catch (Exception e) {
            logger.error("Error fetching user with id {}: {}", id, e.getMessage());
            throw new ServiceException("Error fetching user", e);
        }
    }    

    @Transactional
    public void deleteUser(String idString) {
        logger.info("Attempting to delete user with id: {}", idString);

        if (idString == null || idString.trim().isEmpty()) {
            logger.error("Attempt to delete user with null or empty id");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        ObjectId id = new ObjectId(idString);

        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                logger.info("User with id {} successfully deleted", id);
            } else {
                logger.warn("User with id {} not found for deletion", id);
                throw new ResourceNotFoundException("User not found with id: " + id);
            }
        } catch (ResourceNotFoundException e) {
            throw e;  
        } catch (Exception e) {
            logger.error("Error occurred while deleting user with id {}: {}", id, e.getMessage());
            throw new ServiceException("Failed to delete user", e);
        }
    }

    public List<ObjectId> getAllUserIds() {
        try {
            List<ObjectId> userIds = userRepository.findAllBy();
            logger.info("Retrieved {} user IDs", userIds.size());
            return userIds;
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving user IDs", e);
            throw new ServiceException("An unexpected error occurred while retrieving user IDs", e);
        }
    }
    
    private void validateUserData(User user, boolean isNewUser) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (isNewUser && (user.getPassword() == null || user.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Password is required");
        }        
    }
    
    private void validateUserData(User user) {
        validateUserData(user, false);
    }

}