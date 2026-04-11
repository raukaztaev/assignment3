package com.example.mvp.dto.order;

import java.time.OffsetDateTime;

public record OrderResponse(
        Long id,
        Long planId,
        String status,
        String startedBy,
        OffsetDateTime startedAt,
        String releasedBy,
        OffsetDateTime releasedAt
) {
}
