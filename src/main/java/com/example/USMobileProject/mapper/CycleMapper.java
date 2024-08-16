package com.example.USMobileProject.mapper;

import com.example.USMobileProject.entity.Cycle;
import com.example.USMobileProject.model.CycleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.bson.types.ObjectId;

@Mapper(componentModel = "spring")
public interface CycleMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "objectIdToString")
    Cycle toEntity(CycleModel cycleModel);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "stringToObjectId")
    CycleModel toModel(Cycle cycle);
    
    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }

    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        return id != null ? new ObjectId(id) : null;
    }    
}