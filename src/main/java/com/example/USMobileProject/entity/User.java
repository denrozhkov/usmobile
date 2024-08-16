package com.example.USMobileProject.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
//import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // Constructor without password for GET and PUT 
    public User(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    @Override //excluding password
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }    
}