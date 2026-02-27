package com.juliy.store.backend.metrics.service;

import com.juliy.store.backend.metrics.dto.DashboardMetricsResponse;
import com.juliy.store.backend.metrics.repository.DailySalesProjection;
import com.juliy.store.backend.metrics.repository.SalesSummaryProjection;
import com.juliy.store.backend.metrics.repository.TopProductProjection;
import com.juliy.store.backend.sale.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private MetricsService metricsService;

    @Test
    void shouldBuildDashboardMetricsFromRepositoryData() {
        SalesSummaryProjection summary = org.mockito.Mockito.mock(SalesSummaryProjection.class);
        when(summary.getTotalRevenueMonth()).thenReturn(new BigDecimal("1000.50"));
        when(summary.getTotalSalesMonth()).thenReturn(10L);
        when(summary.getTotalQuantityMonth()).thenReturn(35L);

        DailySalesProjection daily = org.mockito.Mockito.mock(DailySalesProjection.class);
        when(daily.getSaleDate()).thenReturn(LocalDate.of(2026, 2, 27));
        when(daily.getTotalRevenue()).thenReturn(new BigDecimal("200.00"));
        when(daily.getTotalQuantity()).thenReturn(7L);
        when(daily.getTotalSales()).thenReturn(2L);

        TopProductProjection topProduct = org.mockito.Mockito.mock(TopProductProjection.class);
        when(topProduct.getProductId()).thenReturn(1);
        when(topProduct.getProductName()).thenReturn("Producto A");
        when(topProduct.getTotalQuantity()).thenReturn(12L);
        when(topProduct.getTotalRevenue()).thenReturn(new BigDecimal("600.00"));

        when(saleRepository.findSalesSummaryCurrentMonth()).thenReturn(Optional.of(summary));
        when(saleRepository.findDailySalesLast7Days()).thenReturn(List.of(daily));
        when(saleRepository.findTopProductsCurrentMonth()).thenReturn(List.of(topProduct));

        DashboardMetricsResponse response = metricsService.getDashboardMetrics();

        assertEquals(new BigDecimal("1000.50"), response.summary().totalRevenueMonth());
        assertEquals(10L, response.summary().totalSalesMonth());
        assertEquals(35L, response.summary().totalQuantityMonth());
        assertEquals(1, response.dailySales().size());
        assertEquals(1, response.topProducts().size());
        assertEquals("Producto A", response.topProducts().get(0).productName());
    }
}
