package com.juliy.store.backend.product.repository;

import com.juliy.store.backend.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
