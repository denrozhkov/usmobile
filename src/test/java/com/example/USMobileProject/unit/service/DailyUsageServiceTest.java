package com.example.USMobileProject.service;

import com.example.USMobileProject.dto.DailyUsageDTO;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import com.example.USMobileProject.mapper.DailyUsageMapper;
import com.example.USMobileProject.model.CycleModel;
import com.example.USMobileProject.model.DailyUsageModel;
import com.example.USMobileProject.repository.CycleRepository;
import com.example.USMobileProject.repository.DailyUsageRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyUsageServiceTest {

    @Mock
    private CycleRepository cycleRepository;

    @Mock
    private DailyUsageRepository dailyUsageRepository;

    @Mock
    private DailyUsageMapper dailyUsageMapper;

    @InjectMocks
    private DailyUsageService dailyUsageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentCycleDailyUsage_Success() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";
        Date startDate = new Date();
        CycleModel currentCycle = new CycleModel();
        currentCycle.setStartDate(startDate);

        List<DailyUsageModel> usages = Arrays.asList(
            new DailyUsageModel(),
            new DailyUsageModel()
        );

        when(cycleRepository.findTopByUserIdAndMdnOrderByStartDateDesc(userId, mdn)).thenReturn(currentCycle);
        when(dailyUsageRepository.findCurrentCycleDailyUsage(userId, mdn, startDate)).thenReturn(usages);
        when(dailyUsageMapper.modelToDto(any())).thenReturn(new DailyUsageDTO());

        List<DailyUsageDTO> result = dailyUsageService.getCurrentCycleDailyUsage(userId.toString(), mdn);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cycleRepository).findTopByUserIdAndMdnOrderByStartDateDesc(userId, mdn);
        verify(dailyUsageRepository).findCurrentCycleDailyUsage(userId, mdn, startDate);
        verify(dailyUsageMapper, times(2)).modelToDto(any());
    }

    @Test
    void getCurrentCycleDailyUsage_NoCycleFound() {
        ObjectId userId = new ObjectId();
        String mdn = "1234567890";

        when(cycleRepository.findTopByUserIdAndMdnOrderByStartDateDesc(userId, mdn)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> 
            dailyUsageService.getCurrentCycleDailyUsage(userId.toString(), mdn)
        );
    }

}