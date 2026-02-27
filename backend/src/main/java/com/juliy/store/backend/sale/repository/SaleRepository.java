package com.juliy.store.backend.sale.repository;

import com.juliy.store.backend.metrics.repository.DailySalesProjection;
import com.juliy.store.backend.metrics.repository.SalesSummaryProjection;
import com.juliy.store.backend.metrics.repository.TopProductProjection;
import com.juliy.store.backend.sale.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Integer> {

    @Query(value = """
            SELECT
                rv.id_venta AS id,
                p.id_producto AS productId,
                p.nombre AS productName,
                rv.cantidad AS quantity,
                rv.fecha_venta AS saleDate,
                p.precio AS unitPrice,
                (rv.cantidad * p.precio) AS subtotal,
                rv.creado_por AS createdBy,
                rv.creado_en AS createdAt,
                rv.actualizado_en AS updatedAt
            FROM registro_ventas rv
            INNER JOIN productos p ON rv.id_producto = p.id_producto
            ORDER BY rv.id_venta DESC
            """, nativeQuery = true)
    List<SaleReportProjection> findSalesReport();

    @Query(value = """
            SELECT
                rv.id_venta AS id,
                p.id_producto AS productId,
                p.nombre AS productName,
                rv.cantidad AS quantity,
                rv.fecha_venta AS saleDate,
                p.precio AS unitPrice,
                (rv.cantidad * p.precio) AS subtotal,
                rv.creado_por AS createdBy,
                rv.creado_en AS createdAt,
                rv.actualizado_en AS updatedAt
            FROM registro_ventas rv
            INNER JOIN productos p ON rv.id_producto = p.id_producto
            ORDER BY rv.creado_en DESC
            LIMIT 20
            """, nativeQuery = true)
    List<SaleReportProjection> findRecentActivity();

        @Query(value = """
                        SELECT
                                COALESCE(SUM(rv.cantidad * p.precio), 0) AS totalRevenueMonth,
                                COALESCE(COUNT(rv.id_venta), 0) AS totalSalesMonth,
                                COALESCE(SUM(rv.cantidad), 0) AS totalQuantityMonth
                        FROM registro_ventas rv
                        INNER JOIN productos p ON rv.id_producto = p.id_producto
                        WHERE YEAR(rv.fecha_venta) = YEAR(CURDATE())
                            AND MONTH(rv.fecha_venta) = MONTH(CURDATE())
                        """, nativeQuery = true)
        Optional<SalesSummaryProjection> findSalesSummaryCurrentMonth();

        @Query(value = """
                        SELECT
                                rv.fecha_venta AS saleDate,
                                COALESCE(SUM(rv.cantidad * p.precio), 0) AS totalRevenue,
                                COALESCE(SUM(rv.cantidad), 0) AS totalQuantity,
                                COALESCE(COUNT(rv.id_venta), 0) AS totalSales
                        FROM registro_ventas rv
                        INNER JOIN productos p ON rv.id_producto = p.id_producto
                        WHERE rv.fecha_venta >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
                        GROUP BY rv.fecha_venta
                        ORDER BY rv.fecha_venta DESC
                        """, nativeQuery = true)
        List<DailySalesProjection> findDailySalesLast7Days();

        @Query(value = """
                        SELECT
                                p.id_producto AS productId,
                                p.nombre AS productName,
                                COALESCE(SUM(rv.cantidad), 0) AS totalQuantity,
                                COALESCE(SUM(rv.cantidad * p.precio), 0) AS totalRevenue
                        FROM registro_ventas rv
                        INNER JOIN productos p ON rv.id_producto = p.id_producto
                        WHERE YEAR(rv.fecha_venta) = YEAR(CURDATE())
                            AND MONTH(rv.fecha_venta) = MONTH(CURDATE())
                        GROUP BY p.id_producto, p.nombre
                        ORDER BY totalQuantity DESC
                        LIMIT 5
                        """, nativeQuery = true)
        List<TopProductProjection> findTopProductsCurrentMonth();
}
