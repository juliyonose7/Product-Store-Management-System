package com.juliy.store.backend.sale.service;

import com.juliy.store.backend.product.domain.Product;
import com.juliy.store.backend.product.repository.ProductRepository;
import com.juliy.store.backend.sale.domain.Sale;
import com.juliy.store.backend.sale.dto.CreateSaleRequest;
import com.juliy.store.backend.sale.dto.SaleResponse;
import com.juliy.store.backend.sale.repository.SaleRepository;
import com.juliy.store.backend.sale.repository.SaleReportProjection;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findSalesReport()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + request.productId() + " not found"));

        if (product.getStock() < request.quantity()) {
            throw new IllegalArgumentException("Insufficient stock. Current stock: " + product.getStock());
        }

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setQuantity(request.quantity());
        sale.setSaleDate(request.saleDate() == null ? LocalDate.now() : request.saleDate());

        Sale savedSale = saleRepository.save(sale);

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        return new SaleResponse(
                savedSale.getId(),
                product.getId(),
                product.getName(),
                savedSale.getQuantity(),
                savedSale.getSaleDate(),
                product.getPrice(),
                subtotal
        );
    }

    private SaleResponse toResponse(SaleReportProjection projection) {
        return new SaleResponse(
                projection.getId(),
                projection.getProductId(),
                projection.getProductName(),
                projection.getQuantity(),
                projection.getSaleDate(),
                projection.getUnitPrice(),
                projection.getSubtotal()
        );
    }
}
