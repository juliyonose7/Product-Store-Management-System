package com.juliy.store.backend.metrics.controller;

import com.juliy.store.backend.metrics.dto.DashboardMetricsResponse;
import com.juliy.store.backend.metrics.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/dashboard")
    public DashboardMetricsResponse getDashboardMetrics() {
        return metricsService.getDashboardMetrics();
    }
}
