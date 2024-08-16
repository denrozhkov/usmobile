package com.example.USMobileProject;

import com.example.USMobileProject.entity.User;
import com.example.USMobileProject.repository.UserRepository;
import com.example.USMobileProject.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.USMobileProject.exception.ResourceNotFoundException;
import org.bson.types.ObjectId;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class UserIntegrationTest {

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

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testUserCRUDOperations() {
        // Create a user
        User user = new User();
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("myname@yahoo.com");
        user.setPassword("password123");

        User createdUser = userService.createUser(user);
        assertNotNull(createdUser.getId());
        assertEquals("firstName", createdUser.getFirstName());
        assertEquals("lastName", createdUser.getLastName());
        assertEquals("myname@yahoo.com", createdUser.getEmail());

        // Retrieve the user
        User retrievedUser = userService.getUserById(createdUser.getId());
        assertNotNull(retrievedUser);
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals(createdUser.getEmail(), retrievedUser.getEmail());

        // Update the user
        retrievedUser.setFirstName("newName");
        User updatedUser = userService.updateUser(retrievedUser.getId(), retrievedUser);
        assertEquals("newName", updatedUser.getFirstName());
        assertEquals("lastName", updatedUser.getLastName());

        // Delete the user
        userService.deleteUser(updatedUser.getId());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(updatedUser.getId()));

        // Verify on repository level that the user is deleted 
        assertTrue(userRepository.findById(new ObjectId(updatedUser.getId())).isEmpty());
    }
}