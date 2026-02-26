package com.juliy.store.backend.sale.repository;

import com.juliy.store.backend.sale.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Integer> {

    @Query(value = """
            SELECT
                rv.id_venta AS id,
                p.id_producto AS productId,
                p.nombre AS productName,
                rv.cantidad AS quantity,
                rv.fecha_venta AS saleDate,
                p.precio AS unitPrice,
                (rv.cantidad * p.precio) AS subtotal
            FROM registro_ventas rv
            INNER JOIN productos p ON rv.id_producto = p.id_producto
            ORDER BY rv.id_venta DESC
            """, nativeQuery = true)
    List<SaleReportProjection> findSalesReport();
}
