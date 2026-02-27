package com.juliy.store.backend.metrics.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySalesProjection {
    LocalDate getSaleDate();
    BigDecimal getTotalRevenue();
    Long getTotalQuantity();
    Long getTotalSales();
}
