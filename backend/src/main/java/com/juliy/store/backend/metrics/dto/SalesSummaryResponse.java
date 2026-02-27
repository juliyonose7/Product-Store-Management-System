package com.juliy.store.backend.metrics.dto;

import java.math.BigDecimal;

public record SalesSummaryResponse(
        BigDecimal totalRevenueMonth,
        Long totalSalesMonth,
        Long totalQuantityMonth
) {
}
