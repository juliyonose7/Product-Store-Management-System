package com.juliy.store.backend.metrics.repository;

import java.math.BigDecimal;

public interface TopProductProjection {
    Integer getProductId();
    String getProductName();
    Long getTotalQuantity();
    BigDecimal getTotalRevenue();
}
