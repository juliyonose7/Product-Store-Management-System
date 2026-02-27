package com.juliy.store.backend.metrics.dto;

import java.util.List;

public record DashboardMetricsResponse(
        SalesSummaryResponse summary,
        List<DailySalesMetricResponse> dailySales,
        List<TopProductMetricResponse> topProducts
) {
}
