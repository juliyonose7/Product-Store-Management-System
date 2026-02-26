package com.juliy.store.backend.sale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSaleRequest(
        @NotNull @Min(1) Integer productId,
        @NotNull @Min(1) Integer quantity,
        LocalDate saleDate
) {
}
