package com.example.USMobileProject.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class UserIdMdnPair {
    private ObjectId userId;
    private String mdn;
}