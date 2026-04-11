package com.example.mvp.dto.order;

import java.time.OffsetDateTime;

public record OperationResponse(
        Long id,
        Long orderId,
        String operationName,
        Integer completedQuantity,
        String performedBy,
        OffsetDateTime performedAt
) {
}
