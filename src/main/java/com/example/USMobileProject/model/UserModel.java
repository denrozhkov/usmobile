package com.example.USMobileProject.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Users")
public class UserModel {
    @Id
    private ObjectId id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    
    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + (id != null ? id.toHexString() : "null") +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }    
}