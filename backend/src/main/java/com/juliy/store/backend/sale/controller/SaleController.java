package com.juliy.store.backend.sale.controller;

import com.juliy.store.backend.sale.dto.CreateSaleRequest;
import com.juliy.store.backend.sale.dto.SaleResponse;
import com.juliy.store.backend.sale.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<SaleResponse> getSales() {
        return saleService.findAll();
    }

    @GetMapping("/activity")
    public List<SaleResponse> getSalesActivity() {
        return saleService.findRecentActivity();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(@Valid @RequestBody CreateSaleRequest request) {
        return saleService.createSale(request);
    }
}