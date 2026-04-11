package com.example.mvp.dto.plan;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PlanResponse(
        Long id,
        String productCode,
        String productName,
        Integer plannedQuantity,
        LocalDate plannedDate,
        String status,
        String createdBy,
        String updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
