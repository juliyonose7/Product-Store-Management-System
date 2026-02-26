package com.juliy.store.backend.sale.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SaleReportProjection {
    Integer getId();
    Integer getProductId();
    String getProductName();
    Integer getQuantity();
    LocalDate getSaleDate();
    BigDecimal getUnitPrice();
    BigDecimal getSubtotal();
}
