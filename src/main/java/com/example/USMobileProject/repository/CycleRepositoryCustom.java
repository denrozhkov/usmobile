package com.example.USMobileProject.repository;

import com.example.USMobileProject.model.CycleModel;
import java.util.List;

public interface CycleRepositoryCustom {
    int saveBatch(List<CycleModel> cycleBatch);
}