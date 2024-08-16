package com.example.USMobileProject.controller;

import org.bson.types.ObjectId;
import com.example.USMobileProject.dto.ApiResponse;
import com.example.USMobileProject.dto.CycleHistoryDTO;
import com.example.USMobileProject.service.CycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/cycles")
public class CycleController {

    private final CycleService cycleService;

    @Autowired
    public CycleController(CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CycleHistoryDTO>>> getCycleHistory(
            @RequestParam String userId,
            @RequestParam String mdn) {
                
        if (!ObjectId.isValid(userId)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid userId format"));
        }
        
        try {
            List<CycleHistoryDTO> cycleHistory = cycleService.getCycleHistory(userId, mdn);
            ApiResponse<List<CycleHistoryDTO>> response;
            if (cycleHistory.size() == 0) {
                response = new ApiResponse<>(true, "No history for this combination of userId and mdn", cycleHistory);
            } else {
                response = new ApiResponse<>(true, "Cycle history retrieved successfully", cycleHistory);
            }
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred while retrieving cycle history"));
        }
    }
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Integer>> generateCycles() {
        int createdCount = cycleService.generateCycles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Cycles created successfully", createdCount));
    }   
    
}
