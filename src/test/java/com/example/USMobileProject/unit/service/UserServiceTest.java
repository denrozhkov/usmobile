package com.example.USMobileProject.service;

import com.example.USMobileProject.entity.User;
import com.example.USMobileProject.model.UserModel;
import com.example.USMobileProject.repository.UserRepository;
import com.example.USMobileProject.mapper.UserMapper;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import com.example.USMobileProject.exception.ServiceException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "maxUsersNumber", 10000);
        ReflectionTestUtils.setField(userService, "batchSize", 1000);
    }

    @Test
    void createUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("password123");

        UserModel userModel = new UserModel();
        userModel.setId(new ObjectId());

        when(userMapper.toModel(user)).thenReturn(userModel);
        when(userRepository.save(userModel)).thenReturn(userModel);
        when(userMapper.toEntityWithoutPassword(userModel)).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        verify(userRepository).save(userModel);
    }

    @Test
    void createUser_NullUser() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
    }

    @Test
    void updateUser_Success() {
        String userId = new ObjectId().toHexString();
        User user = new User();
        user.setEmail("updated@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");

        UserModel existingUserModel = new UserModel();
        existingUserModel.setId(new ObjectId(userId));

        when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(existingUserModel));
        when(userRepository.save(existingUserModel)).thenReturn(existingUserModel);
        when(userMapper.toEntityWithoutPassword(existingUserModel)).thenReturn(user);

        User updatedUser = userService.updateUser(userId, user);

        assertNotNull(updatedUser);
        assertEquals("Jane", updatedUser.getFirstName());
        verify(userRepository).save(existingUserModel);
    }

    @Test
    void updateUser_UserNotFound() {
        String userId = new ObjectId("507f1f77bcf86cd799439011").toHexString();
        User user = new User();

        when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId, user));
    }

    @Test
    void getUserById_Success() {
        String userId = new ObjectId().toHexString();
        UserModel userModel = new UserModel();
        User user = new User();

        when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(userModel));
        when(userMapper.toEntityWithoutPassword(userModel)).thenReturn(user);

        User retrievedUser = userService.getUserById(userId);

        assertNotNull(retrievedUser);
    }

    @Test
    void getUserById_UserNotFound() {
        String userId = new ObjectId().toHexString();

        when(userRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void deleteUser_Success() {
        String userId = new ObjectId().toHexString();

        when(userRepository.existsById(any(ObjectId.class))).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(userId));
        verify(userRepository).deleteById(any(ObjectId.class));
    }

    @Test
    void deleteUser_UserNotFound() {
        String userId = new ObjectId().toHexString();

        when(userRepository.existsById(any(ObjectId.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void getAllUserIds_Success() {
        List<ObjectId> userIds = Arrays.asList(new ObjectId(), new ObjectId());

        when(userRepository.findAllBy()).thenReturn(userIds);

        List<ObjectId> retrievedIds = userService.getAllUserIds();

        assertEquals(userIds.size(), retrievedIds.size());
        assertEquals(userIds, retrievedIds);
    }

}