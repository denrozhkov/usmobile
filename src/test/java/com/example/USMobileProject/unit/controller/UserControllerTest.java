package com.example.USMobileProject.controller;

import com.example.USMobileProject.dto.ApiResponse;
import com.example.USMobileProject.entity.User;
import com.example.USMobileProject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.USMobileProject.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_Success() {
        User user = new User();
        user.setEmail("test@gmail.com");
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userController.createUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertTrue(apiResponse.isSuccess());
        assertEquals("User created successfully", apiResponse.getMessage());
        assertEquals(user, apiResponse.getData());
    }
    
    @Test
    void updateUser_Success() {
        String userId = "123";
        User user = new User();
        user.setEmail("updated@gmail.com");
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(user);

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(userId, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User updated successfully", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    void getUserById_Success() {
        String userId = "123";
        User user = new User();
        user.setEmail("test@gmail.com");
        when(userService.getUserById(userId)).thenReturn(user);

        ResponseEntity<ApiResponse<User>> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User retrieved successfully", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    void deleteUser_Success() {
        String userId = "123";
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User successfully deleted", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
    
    @Test
    void createUser_Failure() {
        User user = new User();
        user.setEmail("test@gmail.com");
        when(userService.createUser(any(User.class))).thenThrow(new IllegalArgumentException("Invalid user data"));
    
        ResponseEntity<ApiResponse<User>> response = userController.createUser(user);
    
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid user data", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    } 
    
    @Test
    void generateUsers_Failure() {
        when(userService.generateUsers(anyInt())).thenThrow(new RuntimeException("Failed to generate users"));

        ResponseEntity<ApiResponse<Integer>> response = userController.generateUsers(10);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<Integer> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("An unexpected error occurred", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void updateUser_Failure_UserNotFound() {
        String userId = "nonexistent-id";
        User user = new User();
        when(userService.updateUser(eq(userId), any(User.class)))
            .thenThrow(new ResourceNotFoundException("User not found"));

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(userId, user);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("User not found", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void updateUser_Failure_InvalidData() {
        String userId = "valid-id";
        User user = new User();
        when(userService.updateUser(eq(userId), any(User.class)))
            .thenThrow(new IllegalArgumentException("Invalid user data"));

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(userId, user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid user data", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void getUserById_Failure() {
        String userId = "nonexistent-id";
        when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("User not found"));

        ResponseEntity<ApiResponse<User>> response = userController.getUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("User not found", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void deleteUser_Failure() {
        String userId = "nonexistent-id";
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(userId);

        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("User not found", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }    
}