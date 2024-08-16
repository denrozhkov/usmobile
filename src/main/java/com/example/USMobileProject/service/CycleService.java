package com.example.USMobileProject.service;

import com.github.javafaker.Faker;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.ZoneId;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.example.USMobileProject.repository.CycleRepository;
import com.example.USMobileProject.dto.CycleHistoryDTO;
import org.bson.Document;
import com.example.USMobileProject.service.UserService;
import com.example.USMobileProject.mapper.CycleMapper;
import com.example.USMobileProject.model.CycleModel;
import com.example.USMobileProject.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import com.example.USMobileProject.exception.ServiceException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class CycleService {

    private final CycleRepository cycleRepository;
    private final CycleMapper cycleMapper;

    @Autowired
    public CycleService(CycleRepository cycleRepository, CycleMapper cycleMapper) {
        this.cycleRepository = cycleRepository;
        this.cycleMapper = cycleMapper;
    }
    
    private static final Logger logger = LoggerFactory.getLogger(CycleService.class);

    @Value("${app.batch-size:1000}")
    private int batchSize;

    @Autowired
    private UserService userService;    

    public List<CycleHistoryDTO> getCycleHistory(String userIdString, String mdn) {
        ObjectId userId = new ObjectId(userIdString);
        List<CycleModel> cycles = cycleRepository.findByUserIdAndMdnOrderByStartDateDesc(userId, mdn);
        return cycles.stream()
                .map(cycle -> new CycleHistoryDTO(cycle.getId().toHexString(), cycle.getStartDate(), cycle.getEndDate()))
                .collect(Collectors.toList());
    }
    
    
    @Transactional
    public int generateCycles() {
        final LocalDate GEN_START_DATE = LocalDate.of(2024, 1, 1);
        
        Faker faker = new Faker();
        int totalInserted = 0;
        int failedInserts = 0;
        List<ObjectId> allUserIds = userService.getAllUserIds();
        
        int numberOfUsers = allUserIds.size();
        logger.info("Retrieved {} users for cycle generation", numberOfUsers);
        
        LocalDate startDate = GEN_START_DATE; 
        LocalDate endDate = LocalDate.now();
        
        List<CycleModel> cycleBatch = new ArrayList<>();
        
        try {
            for (ObjectId userId : allUserIds) {
                String phoneNumber = faker.numerify("##########");
                LocalDate cycleStart = startDate.plusDays(faker.number().numberBetween(0, 31));
                
                while (!cycleStart.isAfter(endDate)) {
                    LocalDate cycleEnd = cycleStart.plusDays(30);
                    
                    CycleModel cycle = new CycleModel();
                    cycle.setUserId(userId);
                    cycle.setMdn(phoneNumber);
                    cycle.setStartDate(Date.from(cycleStart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    cycle.setEndDate(Date.from(cycleEnd.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    
                    cycleBatch.add(cycle);
                    
                    if (cycleBatch.size() >= batchSize) {
                        try {
                            totalInserted += cycleRepository.saveBatch(cycleBatch);
                            logger.info("Cycles inserted: {}", totalInserted);
                        } catch (Exception e) {
                            logger.error("Error during bulk insert: {}", e.getMessage());
                            failedInserts += cycleBatch.size();
                        } finally {
                            cycleBatch.clear();
                        }                            
                    }
                    
                    cycleStart = cycleEnd.plusDays(1);
                }
            }
            
            if (!cycleBatch.isEmpty()) {
                try {
                    totalInserted += cycleRepository.saveBatch(cycleBatch);
                    logger.info("Cycles inserted: {}", totalInserted);
                } catch (Exception e) {
                    logger.error("Error during bulk insert: {}", e.getMessage());
                    failedInserts += cycleBatch.size();
                } finally {
                    cycleBatch.clear();
                }    
            }
            
            logger.info("Cycles generation completed. Total inserted: {}, Failed inserts: {}", totalInserted, failedInserts);
        } catch (Exception e) {
            logger.error("Error during cycles generation: {}", e.getMessage());
            throw new RuntimeException("Failed to generate cycles", e);
        }
        
        return totalInserted;
    }
    
}