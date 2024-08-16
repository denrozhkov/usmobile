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
@Document(collection = "Cycles")
public class CycleModel {
    @Id
    private ObjectId id;

    @Indexed
    private String mdn;

    private Date startDate;

    private Date endDate;

    @Indexed
    private ObjectId userId;
}