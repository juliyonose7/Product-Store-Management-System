package com.juliy.store.backend.metrics.dto;

import java.math.BigDecimal;

public record TopProductMetricResponse(
        Integer productId,
        String productName,
        Long totalQuantity,
        BigDecimal totalRevenue
) {
}
