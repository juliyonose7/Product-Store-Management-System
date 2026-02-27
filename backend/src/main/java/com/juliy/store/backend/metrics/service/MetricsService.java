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
import java.util.StringJoiner;

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

        @Transactional(readOnly = true)
        public byte[] exportMetricsCsv() {
                DashboardMetricsResponse metrics = getDashboardMetrics();

                StringBuilder csv = new StringBuilder();
                csv.append("section,key,value\n");
                csv.append("summary,totalRevenueMonth,").append(metrics.summary().totalRevenueMonth()).append("\n");
                csv.append("summary,totalSalesMonth,").append(metrics.summary().totalSalesMonth()).append("\n");
                csv.append("summary,totalQuantityMonth,").append(metrics.summary().totalQuantityMonth()).append("\n\n");

                csv.append("dailySales,saleDate,totalRevenue,totalQuantity,totalSales\n");
                for (DailySalesMetricResponse day : metrics.dailySales()) {
                        StringJoiner row = new StringJoiner(",");
                        row.add("dailySales");
                        row.add(String.valueOf(day.saleDate()));
                        row.add(String.valueOf(day.totalRevenue()));
                        row.add(String.valueOf(day.totalQuantity()));
                        row.add(String.valueOf(day.totalSales()));
                        csv.append(row).append("\n");
                }

                csv.append("\n");
                csv.append("topProducts,productId,productName,totalQuantity,totalRevenue\n");
                for (TopProductMetricResponse product : metrics.topProducts()) {
                        StringJoiner row = new StringJoiner(",");
                        row.add("topProducts");
                        row.add(String.valueOf(product.productId()));
                        row.add(escapeCsv(product.productName()));
                        row.add(String.valueOf(product.totalQuantity()));
                        row.add(String.valueOf(product.totalRevenue()));
                        csv.append(row).append("\n");
                }

                return csv.toString().getBytes();
        }

        private String escapeCsv(String value) {
                if (value == null) {
                        return "";
                }
                return "\"" + value.replace("\"", "\"\"") + "\"";
        }
}
