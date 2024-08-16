package com.example.USMobileProject.service;

import com.example.USMobileProject.model.CycleModel;
import com.example.USMobileProject.model.DailyUsageModel;
import com.example.USMobileProject.repository.CycleRepository;
import com.example.USMobileProject.repository.DailyUsageRepository;
import com.example.USMobileProject.mapper.DailyUsageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.USMobileProject.dto.DailyUsageDTO;
import com.example.USMobileProject.dto.UserIdMdnPair;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import com.example.USMobileProject.exception.ServiceException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.bson.types.ObjectId;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DailyUsageService {

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private DailyUsageRepository dailyUsageRepository;
    
    @Autowired
    private DailyUsageMapper dailyUsageMapper;
    
    private static final Logger logger = LoggerFactory.getLogger(DailyUsageService.class);

    private static final int BATCH_SIZE = 1000;
    private static final Random RANDOM = new Random();

    public List<DailyUsageDTO> getCurrentCycleDailyUsage(String userIdString, String mdn) {
        ObjectId userId = new ObjectId(userIdString);
        
        CycleModel currentCycle;
        try {
            currentCycle = cycleRepository.findTopByUserIdAndMdnOrderByStartDateDesc(userId, mdn);
        } catch (Exception e) {
            logger.error("Error fetching current cycle for user {} and mdn {}: {}", userId, mdn, e.getMessage());
            throw new ServiceException("Error fetching current cycle", e);
        }
        
        if (currentCycle == null) {
            logger.warn("No current cycle found for user {} and mdn {}", userId, mdn);
            throw new ResourceNotFoundException("No current cycle found for the given user and MDN");
        }

        logger.info("Current cycle for user {} and mdn {} is {}", userId, mdn, currentCycle);
        
        Date startDate = currentCycle.getStartDate();
        List<DailyUsageModel> usages;
        
        try {
            usages = dailyUsageRepository.findCurrentCycleDailyUsage(userId, mdn, startDate);
        } catch (Exception e) {
            logger.error("Error fetching daily usage for user {} and mdn {}: {}", userId, mdn, e.getMessage());
            throw new ServiceException("Error fetching daily usage", e);
        }
        
        return usages.stream()
                .map(dailyUsageMapper::modelToDto)
                .collect(Collectors.toList());
    }
    
    public int generateDailyUsage() {
        // Fetch distinct user IDs and MDNs
        final LocalDate START_DATE = LocalDate.now().minusDays(50);
        final LocalDate END_DATE = LocalDate.now();
    
        logger.info("Generating daily usage data");
        List<UserIdMdnPair> distinctPairs = cycleRepository.findDistinctUserIdsAndMdns();
        logger.info("Found {} user-mdn pairs", distinctPairs.size());
        
        List<DailyUsageModel> dailyUsages = new ArrayList<>(BATCH_SIZE);
        int totalInserted = 0;
        int failedInserts = 0;        

        for (UserIdMdnPair pair : distinctPairs) {
            ObjectId userId = pair.getUserId();
            String mdn = pair.getMdn();
            
            LocalDate usageDate = START_DATE;
            while (!usageDate.isAfter(END_DATE)) {
                DailyUsageModel dailyUsage = new DailyUsageModel();
                dailyUsage.setMdn(mdn);
                dailyUsage.setUserId(userId);
                dailyUsage.setUsageDate(Date.from(usageDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                dailyUsage.setUsedInMb(roundToTwoDecimals(RANDOM.nextDouble() * 99 + 1));

                dailyUsages.add(dailyUsage);

                if (dailyUsages.size() >= BATCH_SIZE) {
                    try {
                        totalInserted += dailyUsageRepository.bulkInsertDailyUsage(dailyUsages);
                        logger.info("Daily usage records inserted: {}", totalInserted);
                    } catch (Exception e) {
                        logger.error("Error during bulk insert: {}", e.getMessage());
                        failedInserts += dailyUsages.size();
                    } finally {
                        dailyUsages.clear();
                    }
                }

                usageDate = usageDate.plusDays(1);
            }
        }

        if (!dailyUsages.isEmpty()) {
            try {
                totalInserted += dailyUsageRepository.bulkInsertDailyUsage(dailyUsages);
            } catch (Exception e) {
                logger.error("Error during final bulk insert: {}", e.getMessage());
                failedInserts += dailyUsages.size();
            }
        }

        logger.info("Daily usage generation completed. Total inserted: {}, Failed inserts: {}", totalInserted, failedInserts);

        return totalInserted;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}