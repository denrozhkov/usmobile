package com.example.USMobileProject.service;

import com.example.USMobileProject.model.CycleModel;
import com.example.USMobileProject.repository.CycleRepository;
import com.example.USMobileProject.dto.UserIdMdnPair;
import com.example.USMobileProject.dto.CycleHistoryDTO;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@Testcontainers
class CycleServiceIntegrationTest {

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
    
    @BeforeEach
    void setup() {
        mongoTemplate.dropCollection(CycleModel.class);
        when(userService.getAllUserIds()).thenReturn(Arrays.asList(new ObjectId(), new ObjectId()));
    }    

    @Autowired
    private CycleService cycleService;

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private UserService userService;

    @Test
    void generateCycles_shouldCreateCyclesForAllUsers() {
        int totalInserted = cycleService.generateCycles();

        assertTrue(totalInserted > 0, "Should have inserted cycles");

        List<UserIdMdnPair> distinctPairs = cycleRepository.findDistinctUserIdsAndMdns();
        assertEquals(2, distinctPairs.size(), "Should have created cycles for both users");

        for (UserIdMdnPair pair : distinctPairs) {
            List<CycleModel> userCycles = cycleRepository.findByUserIdAndMdn(pair.getUserId(), pair.getMdn());
            assertFalse(userCycles.isEmpty(), "Each user should have cycles");

            LocalDate previousEndDate = null;
            for (CycleModel cycle : userCycles) {
                if (previousEndDate != null) {
                    assertEquals(previousEndDate.plusDays(1), cycle.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            "Cycles should be continuous");
                }
                previousEndDate = cycle.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
    }

    @Test
    void getCycleHistory_shouldReturnCorrectHistory() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < 3; i++) {
            CycleModel cycle = new CycleModel();
            cycle.setUserId(userId);
            cycle.setMdn(mdn);
            cycle.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cycle.setEndDate(Date.from(startDate.plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cycleRepository.save(cycle);
            startDate = startDate.plusDays(31);
        }

        List<CycleHistoryDTO> history = cycleService.getCycleHistory(userId.toString(), mdn);

        assertEquals(3, history.size(), "Should return 3 cycles");
        assertTrue(history.get(0).getStartDate().after(history.get(1).getStartDate()), "Should be ordered by start date descending");
    }
    
    @Test
    void getCycleHistory_shouldReturnEmptyListWhenNoHistory() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";

        List<CycleHistoryDTO> history = cycleService.getCycleHistory(userId.toString(), mdn);

        assertNotNull(history, "The returned list should not be null");
        assertTrue(history.isEmpty(), "The history list should be empty");
    }    
}