package com.example.USMobileProject.controller;

import com.example.USMobileProject.service.DailyUsageService;
import com.example.USMobileProject.dto.ApiResponse;
import com.example.USMobileProject.dto.DailyUsageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/daily-usage")
public class DailyUsageController {

    @Autowired
    private DailyUsageService dailyUsageService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Integer>> generateUsage() {
        int createdCount = dailyUsageService.generateDailyUsage();
        return ResponseEntity.ok(new ApiResponse<>(true, "Daily usage records created successfully", createdCount));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DailyUsageDTO>>> getCurrentCycleDailyUsage(
            @RequestParam String userId,
            @RequestParam String mdn) {
        try {
            List<DailyUsageDTO> usages = dailyUsageService.getCurrentCycleDailyUsage(userId, mdn);
            ApiResponse<List<DailyUsageDTO>> response = new ApiResponse<>(true, "Daily usage in the current cycle for user " + userId + " and phone number " + mdn, usages);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<List<DailyUsageDTO>> errorResponse = new ApiResponse<>(false, "No current cycle found for the given user and MDN", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<List<DailyUsageDTO>> errorResponse = new ApiResponse<>(false, "Invalid user ID or MDN format", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<List<DailyUsageDTO>> errorResponse = new ApiResponse<>(false, "An error occurred while fetching daily usage data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }        
}