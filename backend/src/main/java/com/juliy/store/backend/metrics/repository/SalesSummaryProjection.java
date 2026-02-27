package com.juliy.store.backend.metrics.repository;

import java.math.BigDecimal;

public interface SalesSummaryProjection {
    BigDecimal getTotalRevenueMonth();
    Long getTotalSalesMonth();
    Long getTotalQuantityMonth();
}
