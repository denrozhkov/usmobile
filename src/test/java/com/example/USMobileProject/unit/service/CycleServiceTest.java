package com.example.USMobileProject.service;

import com.example.USMobileProject.dto.CycleHistoryDTO;
import com.example.USMobileProject.mapper.CycleMapper;
import com.example.USMobileProject.model.CycleModel;
import com.example.USMobileProject.repository.CycleRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CycleServiceTest {

    @Mock
    private CycleRepository cycleRepository;

    @Mock
    private CycleMapper cycleMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private CycleService cycleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(cycleService, "batchSize", 1000);
    }

    @Test
    void getCycleHistory_Success() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";
        List<CycleModel> cycles = Arrays.asList(
            createCycleModel(userId, mdn, LocalDate.now().minusDays(30), LocalDate.now()),
            createCycleModel(userId, mdn, LocalDate.now().minusDays(60), LocalDate.now().minusDays(31))
        );

        when(cycleRepository.findByUserIdAndMdnOrderByStartDateDesc(userId, mdn)).thenReturn(cycles);

        List<CycleHistoryDTO> result = cycleService.getCycleHistory(userId.toString(), mdn);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cycleRepository).findByUserIdAndMdnOrderByStartDateDesc(userId, mdn);
    }

    @Test
    void getCycleHistory_Failure() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";

        when(cycleRepository.findByUserIdAndMdnOrderByStartDateDesc(userId, mdn)).thenReturn(Collections.emptyList());

        List<CycleHistoryDTO> result = cycleService.getCycleHistory(userId.toString(), mdn);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cycleRepository).findByUserIdAndMdnOrderByStartDateDesc(userId, mdn);
    }
    
    private CycleModel createCycleModel(ObjectId userId, String mdn, LocalDate startDate, LocalDate endDate) {
        CycleModel cycle = new CycleModel();
        cycle.setId(new ObjectId());
        cycle.setUserId(userId);
        cycle.setMdn(mdn);
        cycle.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cycle.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return cycle;
    }
}