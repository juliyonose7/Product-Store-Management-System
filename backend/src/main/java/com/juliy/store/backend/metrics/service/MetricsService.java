package com.juliy.store.backend.metrics.service;

import com.juliy.store.backend.metrics.dto.DailySalesMetricResponse;
import com.juliy.store.backend.metrics.dto.DashboardMetricsResponse;
import com.juliy.store.backend.metrics.dto.SalesSummaryResponse;
import com.juliy.store.backend.metrics.dto.TopProductMetricResponse;
import com.juliy.store.backend.sale.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MetricsService {

    private final SaleRepository saleRepository;

    public MetricsService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics() {
        SalesSummaryResponse summary = saleRepository.findSalesSummaryCurrentMonth()
                .map(item -> new SalesSummaryResponse(
                        item.getTotalRevenueMonth() == null ? BigDecimal.ZERO : item.getTotalRevenueMonth(),
                        item.getTotalSalesMonth() == null ? 0L : item.getTotalSalesMonth(),
                        item.getTotalQuantityMonth() == null ? 0L : item.getTotalQuantityMonth()
                ))
                .orElse(new SalesSummaryResponse(BigDecimal.ZERO, 0L, 0L));

        List<DailySalesMetricResponse> daily = saleRepository.findDailySalesLast7Days()
                .stream()
                .map(item -> new DailySalesMetricResponse(
                        item.getSaleDate(),
                        item.getTotalRevenue(),
                        item.getTotalQuantity(),
                        item.getTotalSales()
                ))
                .toList();

        List<TopProductMetricResponse> topProducts = saleRepository.findTopProductsCurrentMonth()
                .stream()
                .map(item -> new TopProductMetricResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getTotalQuantity(),
                        item.getTotalRevenue()
                ))
                .toList();

        return new DashboardMetricsResponse(summary, daily, topProducts);
    }
}
