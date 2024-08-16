package com.example.USMobileProject.controller;

import com.example.USMobileProject.dto.ApiResponse;
import com.example.USMobileProject.dto.DailyUsageDTO;
import com.example.USMobileProject.service.DailyUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyUsageControllerTest {

    @Mock
    private DailyUsageService dailyUsageService;

    @InjectMocks
    private DailyUsageController dailyUsageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentCycleDailyUsage_Success() {
        String userId = "user123";
        String mdn = "1234567890";
        List<DailyUsageDTO> mockUsages = Arrays.asList(
            new DailyUsageDTO(),
            new DailyUsageDTO()
        );

        when(dailyUsageService.getCurrentCycleDailyUsage(userId, mdn)).thenReturn(mockUsages);

        ResponseEntity<ApiResponse<List<DailyUsageDTO>>> response = dailyUsageController.getCurrentCycleDailyUsage(userId, mdn);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Daily usage in the current cycle for user " + userId + " and phone number " + mdn, response.getBody().getMessage());
        assertEquals(mockUsages, response.getBody().getData());
    }

    @Test
    void getCurrentCycleDailyUsage_EmptyResult() {
        String userId = "user123";
        String mdn = "1234567890";
        List<DailyUsageDTO> emptyUsages = List.of();

        when(dailyUsageService.getCurrentCycleDailyUsage(userId, mdn)).thenReturn(emptyUsages);

        ResponseEntity<ApiResponse<List<DailyUsageDTO>>> response = dailyUsageController.getCurrentCycleDailyUsage(userId, mdn);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData().isEmpty());
    }
}