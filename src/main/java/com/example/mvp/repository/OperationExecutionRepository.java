package com.example.mvp.repository;

import com.example.mvp.entity.OperationExecution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperationExecutionRepository extends JpaRepository<OperationExecution, Long> {
    List<OperationExecution> findByOrderId(Long orderId);

    @Query("select coalesce(sum(o.completedQuantity), 0) from OperationExecution o where o.order.id = :orderId")
    int totalCompletedByOrderId(@Param("orderId") Long orderId);
}
