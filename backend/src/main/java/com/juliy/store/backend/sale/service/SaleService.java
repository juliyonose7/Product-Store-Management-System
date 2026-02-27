package com.juliy.store.backend.sale.service;

import com.juliy.store.backend.product.domain.Product;
import com.juliy.store.backend.product.repository.ProductRepository;
import com.juliy.store.backend.sale.domain.Sale;
import com.juliy.store.backend.sale.dto.CreateSaleRequest;
import com.juliy.store.backend.sale.dto.SaleResponse;
import com.juliy.store.backend.sale.repository.SaleRepository;
import com.juliy.store.backend.sale.repository.SaleReportProjection;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

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

    @Transactional(readOnly = true)
    public List<SaleResponse> findRecentActivity() {
        return saleRepository.findRecentActivity()
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
        sale.setCreatedBy(getCurrentUsername());

        Sale savedSale = saleRepository.save(sale);

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        return new SaleResponse(
                savedSale.getId(),
                product.getId(),
                product.getName(),
                savedSale.getQuantity(),
                savedSale.getSaleDate(),
                product.getPrice(),
                subtotal,
                savedSale.getCreatedBy(),
                savedSale.getCreatedAt(),
                savedSale.getUpdatedAt()
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
                projection.getSubtotal(),
                projection.getCreatedBy(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public byte[] exportSalesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("id_venta,id_producto,producto,cantidad,fecha_venta,precio_unitario,subtotal,creado_por,creado_en,actualizado_en\n");

        for (SaleResponse sale : findAll()) {
            StringJoiner row = new StringJoiner(",");
            row.add(String.valueOf(sale.id()));
            row.add(String.valueOf(sale.productId()));
            row.add(escapeCsv(sale.productName()));
            row.add(String.valueOf(sale.quantity()));
            row.add(String.valueOf(sale.saleDate()));
            row.add(String.valueOf(sale.unitPrice()));
            row.add(String.valueOf(sale.subtotal()));
            row.add(escapeCsv(sale.createdBy()));
            row.add(String.valueOf(sale.createdAt()));
            row.add(String.valueOf(sale.updatedAt()));
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
