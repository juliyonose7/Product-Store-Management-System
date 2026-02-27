package com.juliy.store.backend.metrics.controller;

import com.juliy.store.backend.metrics.dto.DashboardMetricsResponse;
import com.juliy.store.backend.metrics.service.MetricsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportMetricsCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=metrics-report.csv")
                .contentType(new MediaType("text", "csv"))
                .body(metricsService.exportMetricsCsv());
    }
}
