package com.example.USMobileProject.controller;

import com.example.USMobileProject.dto.ApiResponse;
import com.example.USMobileProject.entity.User;
import com.example.USMobileProject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.USMobileProject.exception.ResourceNotFoundException;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", createdUser));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse<>(false, e.getMessage(), null));            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }  
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Integer>> generateUsers(@RequestParam int numberOfUsers) {
        if (numberOfUsers <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Number of users must be positive"));
        }
        try {
            int createdCount = userService.generateUsers(numberOfUsers);
            return ResponseEntity.ok(new ApiResponse<>(true, "Users created successfully", createdCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", updatedUser));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", user));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User successfully deleted", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }

/*
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, status);
*/
}