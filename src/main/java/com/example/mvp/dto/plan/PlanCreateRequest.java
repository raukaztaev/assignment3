package com.example.mvp.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PlanCreateRequest(
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^[A-Z0-9_-]{2,32}$", message = "productCode must contain only A-Z, 0-9, _ or -")
        String productCode,

        @NotBlank
        @Size(max = 120)
        String productName,

        @NotNull
        @Min(1)
        @Max(1_000_000)
        Integer plannedQuantity,

        @NotNull
        @FutureOrPresent
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate plannedDate
) {
}
