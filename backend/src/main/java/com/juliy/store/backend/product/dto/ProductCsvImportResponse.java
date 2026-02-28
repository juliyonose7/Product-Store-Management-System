package com.juliy.store.backend.product.dto;

import java.util.List;

public record ProductCsvImportResponse(
        int created,
        int skipped,
        List<String> errors
) {
}
