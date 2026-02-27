package com.juliy.store.backend.metrics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesMetricResponse(
        LocalDate saleDate,
        BigDecimal totalRevenue,
        Long totalQuantity,
        Long totalSales
) {
}
