package com.example.USMobileProject.mapper;

import com.example.USMobileProject.dto.DailyUsageDTO;
import com.example.USMobileProject.model.DailyUsageModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DailyUsageMapper {
    @Mapping(source = "usageDate", target = "date")
    @Mapping(source = "usedInMb", target = "dailyUsage")
    DailyUsageDTO modelToDto(DailyUsageModel model);
}