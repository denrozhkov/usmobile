package com.example.USMobileProject.controller;

import com.example.USMobileProject.dto.ApiResponse;
import com.example.USMobileProject.dto.CycleHistoryDTO;
import com.example.USMobileProject.service.CycleService;
import com.example.USMobileProject.exception.ResourceNotFoundException;
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

class CycleControllerTest {

    private static final String VALID_USERID_STRING = "507f1f77bcf86cd799439011";
    @Mock
    private CycleService cycleService;

    @InjectMocks
    private CycleController cycleController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCycleHistory_Success() {
        String userId = VALID_USERID_STRING; // Valid ObjectId
        String mdn = "1234567890";
        List<CycleHistoryDTO> mockHistory = Arrays.asList(new CycleHistoryDTO(), new CycleHistoryDTO());
        
        when(cycleService.getCycleHistory(userId, mdn)).thenReturn(mockHistory);

        ResponseEntity<ApiResponse<List<CycleHistoryDTO>>> response = cycleController.getCycleHistory(userId, mdn);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Cycle history retrieved successfully", response.getBody().getMessage());
        assertEquals(mockHistory, response.getBody().getData());
    }

    @Test
    void getCycleHistory_InvalidUserId() {
        String userId = "invalid-user-id";
        String mdn = "1234567890";

        ResponseEntity<ApiResponse<List<CycleHistoryDTO>>> response = cycleController.getCycleHistory(userId, mdn);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid userId format", response.getBody().getMessage());
    }

    @Test
    void getCycleHistory_ResourceNotFound() {
        String userId = VALID_USERID_STRING;
        String mdn = "1234567890";
        
        when(cycleService.getCycleHistory(userId, mdn)).thenThrow(new ResourceNotFoundException("Resource not found"));

        ResponseEntity<ApiResponse<List<CycleHistoryDTO>>> response = cycleController.getCycleHistory(userId, mdn);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void getCycleHistory_IllegalArgument() {
        String userId = VALID_USERID_STRING;
        String mdn = "1234567890";
        
        when(cycleService.getCycleHistory(userId, mdn)).thenThrow(new IllegalArgumentException("Invalid argument"));

        ResponseEntity<ApiResponse<List<CycleHistoryDTO>>> response = cycleController.getCycleHistory(userId, mdn);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void getCycleHistory_UnexpectedException() {
        String userId = VALID_USERID_STRING;
        String mdn = "1234567890";
        
        when(cycleService.getCycleHistory(userId, mdn)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<ApiResponse<List<CycleHistoryDTO>>> response = cycleController.getCycleHistory(userId, mdn);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred while retrieving cycle history", response.getBody().getMessage());
    }

}