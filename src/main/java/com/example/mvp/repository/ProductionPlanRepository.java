package com.example.mvp.repository;

import com.example.mvp.entity.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
}
