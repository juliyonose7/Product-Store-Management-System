package com.juliy.store.backend.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SaleResponse(
        Integer id,
        Integer productId,
        String productName,
        Integer quantity,
        LocalDate saleDate,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
