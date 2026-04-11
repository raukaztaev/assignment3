package com.example.mvp.repository;

import com.example.mvp.entity.OrderStatus;
import com.example.mvp.entity.ProductionOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    Optional<ProductionOrder> findByPlanId(Long planId);
    List<ProductionOrder> findByStatusIn(List<OrderStatus> statuses);
}
