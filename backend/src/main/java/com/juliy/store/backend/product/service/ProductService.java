package com.juliy.store.backend.product.service;

import com.juliy.store.backend.product.domain.Product;
import com.juliy.store.backend.product.dto.CreateProductRequest;
import com.juliy.store.backend.product.dto.ProductCsvImportResponse;
import com.juliy.store.backend.product.dto.ProductResponse;
import com.juliy.store.backend.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductCsvImportResponse importFromCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required");
        }

        int created = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (lineNumber == 1 && isHeader(trimmed)) {
                    continue;
                }

                String[] parts = splitCsv(trimmed);
                if (parts.length < 3) {
                    skipped++;
                    errors.add("Line " + lineNumber + ": expected format name,price,stock");
                    continue;
                }

                try {
                    CreateProductRequest request = new CreateProductRequest(
                            cleanValue(parts[0]),
                            new BigDecimal(cleanValue(parts[1])),
                            Integer.parseInt(cleanValue(parts[2]))
                    );
                    validateCsvRequest(request, lineNumber);
                    createProduct(request);
                    created++;
                } catch (Exception ex) {
                    skipped++;
                    errors.add("Line " + lineNumber + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not read CSV file", ex);
        }

        return new ProductCsvImportResponse(created, skipped, errors);
    }

    private void validateCsvRequest(CreateProductRequest request, int lineNumber) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.name().length() > 100) {
            throw new IllegalArgumentException("name too long (max 100)");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("price must be greater than 0");
        }
        if (request.stock() == null || request.stock() < 0) {
            throw new IllegalArgumentException("stock must be 0 or greater");
        }
    }

    private boolean isHeader(String line) {
        String normalized = line.toLowerCase();
        return normalized.contains("name") || normalized.contains("nombre");
    }

    private String[] splitCsv(String line) {
        String delimiter = line.contains(";") ? ";" : ",";
        return line.split(delimiter);
    }

    private String cleanValue(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            return cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}
