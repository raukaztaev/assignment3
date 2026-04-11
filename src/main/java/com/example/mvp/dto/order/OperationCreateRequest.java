package com.example.mvp.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OperationCreateRequest(
        @NotBlank
        @Size(max = 120)
        @Pattern(regexp = "^[\\p{L}0-9 _\\-]{2,120}$", message = "operationName contains invalid characters")
        String operationName,

        @NotNull
        @Min(1)
        @Max(1_000_000)
        Integer completedQuantity
) {
}
