package com.example.USMobileProject.service;

import com.example.USMobileProject.model.CycleModel;
import com.example.USMobileProject.model.DailyUsageModel;
import com.example.USMobileProject.repository.CycleRepository;
import com.example.USMobileProject.repository.DailyUsageRepository;
import com.example.USMobileProject.dto.DailyUsageDTO;
import com.example.USMobileProject.dto.UserIdMdnPair;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class DailyUsageServiceIntegrationTest {

    private static final int MONGODB_DEFAULT_PORT = 27017;
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6")
        .withExposedPorts(MONGODB_DEFAULT_PORT);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> 
            String.format("mongodb://%s:%d/testdb",
                mongoDBContainer.getHost(),
                mongoDBContainer.getMappedPort(MONGODB_DEFAULT_PORT))
        );
    }

    @Autowired
    private DailyUsageService dailyUsageService;

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private DailyUsageRepository dailyUsageRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        mongoTemplate.dropCollection(CycleModel.class);
        mongoTemplate.dropCollection(DailyUsageModel.class);
    }

    @Test
    void getCurrentCycleDailyUsage_shouldReturnUsageForCurrentCycle() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(20);

        CycleModel cycle = new CycleModel();
        cycle.setUserId(userId);
        cycle.setMdn(mdn);
        cycle.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cycle.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cycleRepository.save(cycle);

        for (int i = 0; i < 5; i++) {
            DailyUsageModel usage = new DailyUsageModel();
            usage.setUserId(userId);
            usage.setMdn(mdn);
            usage.setUsageDate(Date.from(startDate.plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            usage.setUsedInMb(10.0);
            dailyUsageRepository.save(usage);
        }

        List<DailyUsageDTO> result = dailyUsageService.getCurrentCycleDailyUsage(userId.toString(), mdn);

        assertEquals(5, result.size());
        for (DailyUsageDTO dto : result) {
            assertEquals(10.0, dto.getDailyUsage());
        }
    }

    @Test
    void getCurrentCycleDailyUsage_shouldThrowExceptionWhenNoCycleFound() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";

        assertThrows(ResourceNotFoundException.class, () -> 
            dailyUsageService.getCurrentCycleDailyUsage(userId.toString(), mdn)
        );
    }

    @Test
    void generateDailyUsage_shouldGenerateUsageForAllUserMdnPairs() {
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        String mdn1 = "1234567890";
        String mdn2 = "0987654321";

        cycleRepository.save(createCycle(userId1, mdn1));
        cycleRepository.save(createCycle(userId2, mdn2));

        int totalInserted = dailyUsageService.generateDailyUsage();

        assertTrue(totalInserted > 0);
        List<UserIdMdnPair> distinctPairs = cycleRepository.findDistinctUserIdsAndMdns();
        assertEquals(2, distinctPairs.size());

        for (UserIdMdnPair pair : distinctPairs) {
            List<DailyUsageModel> usages = dailyUsageRepository.findCurrentCycleDailyUsage(pair.getUserId(), pair.getMdn(), new Date(0));
            assertEquals(51, usages.size()); // 50 days from START_DATE to END_DATE, inclusive
            for (DailyUsageModel usage : usages) {
                assertTrue(usage.getUsedInMb() > 0 && usage.getUsedInMb() <= 100);
            }
        }
    }

    private CycleModel createCycle(ObjectId userId, String mdn) {
        CycleModel cycle = new CycleModel();
        cycle.setUserId(userId);
        cycle.setMdn(mdn);
        cycle.setStartDate(Date.from(LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cycle.setEndDate(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return cycle;
    }
}