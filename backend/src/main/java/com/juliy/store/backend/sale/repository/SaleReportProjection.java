package com.juliy.store.backend.sale.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SaleReportProjection {
    Integer getId();
    Integer getProductId();
    String getProductName();
    Integer getQuantity();
    LocalDate getSaleDate();
    BigDecimal getUnitPrice();
    BigDecimal getSubtotal();
    String getCreatedBy();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
