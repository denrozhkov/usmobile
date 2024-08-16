package com.example.USMobileProject.mapper;

import com.example.USMobileProject.entity.User;
import com.example.USMobileProject.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;
import org.mapstruct.Named;
import org.bson.types.ObjectId;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "password", ignore = true)
    User toEntityWithoutPassword(UserModel userModel);

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    User toEntity(UserModel userModel);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    UserModel toModel(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserModel(@MappingTarget UserModel userModel, User user);
    
    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }

    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        return id != null ? new ObjectId(id) : null;
    }

    @Named("toId")
    default String mapToId(UserModel userModel) {
        return userModel != null && userModel.getId() != null ? userModel.getId().toHexString() : null;
    }

    default List<String> toIdList(List<UserModel> userModels) {
        return userModels.stream()
                .map(this::mapToId)
                .collect(Collectors.toList());
    }
}