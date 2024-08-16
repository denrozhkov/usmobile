package com.example.USMobileProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.Date;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "DailyUsage")
public class DailyUsageModel {
    @Id
    private ObjectId id;

    @Indexed
    private String mdn;
    
    @Indexed
    private ObjectId userId;

    private Date usageDate;

    private double usedInMb;
}