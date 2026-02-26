package com.juliy.store.backend.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        String name,
        BigDecimal price,
        Integer stock
) {
}
