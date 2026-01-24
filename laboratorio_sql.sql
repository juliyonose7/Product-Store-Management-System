-- ============================================================================
-- Laboratory: Cursor programming, functions, stored procedures, and triggers in SQL
-- Database: tienda_productos
-- ============================================================================

-- Create database
DROP DATABASE IF EXISTS tienda_productos;
CREATE DATABASE tienda_productos;
USE tienda_productos;

-- ============================================================================
-- ACTIVITY 1: Table creation and data load
-- ============================================================================

-- Products table
CREATE TABLE productos (
    id_producto INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL
);

-- Insert product data
INSERT INTO productos (nombre, precio, stock) VALUES
('Producto A', 50, 100),
('Producto B', 75, 80),
('Producto C', 60, 120),
('Producto D', 90, 60);

-- Sales log table
CREATE TABLE registro_ventas (
    id_venta INT PRIMARY KEY AUTO_INCREMENT,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    fecha_venta DATE NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

-- ============================================================================
-- ACTIVITY 2: Cursor usage to calculate total sales
-- ============================================================================

DELIMITER //

CREATE PROCEDURE calcular_total_ventas()
BEGIN
    -- Cursor variables
    DECLARE v_id_producto INT;
    DECLARE v_cantidad INT;
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_subtotal DECIMAL(10,2);
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    DECLARE done INT DEFAULT FALSE;
    
    -- Declare cursor
    DECLARE cur_ventas CURSOR FOR
        SELECT rv.id_producto, rv.cantidad, p.precio
        FROM registro_ventas rv
        INNER JOIN productos p ON rv.id_producto = p.id_producto;
    
    -- Declare end of cursor handler
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- Open cursor
    OPEN cur_ventas;
    
    -- Show header
    SELECT 'CÁLCULO DEL TOTAL DE VENTAS' AS Titulo;
    SELECT '================================' AS Separador;
    
    -- Cursor loop
    read_loop: LOOP
        FETCH cur_ventas INTO v_id_producto, v_cantidad, v_precio;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Calculate subtotal
        SET v_subtotal = v_cantidad * v_precio;
        SET v_total = v_total + v_subtotal;
        
        -- Show each sale detail
        SELECT CONCAT('Producto ID: ', v_id_producto, 
                     ' | Cantidad: ', v_cantidad,
                     ' | Precio: $', v_precio,
                     ' | Subtotal: $', v_subtotal) AS Detalle_Venta;
    END LOOP;
    
    -- Close cursor
    CLOSE cur_ventas;
    
    -- Show total
    SELECT '================================' AS Separador;
    SELECT CONCAT('TOTAL DE VENTAS: $', v_total) AS Total_Final;
END //

DELIMITER ;

-- ============================================================================
-- ACTIVITY 3: Function creation
-- ============================================================================

DELIMITER //

CREATE FUNCTION obtener_stock_producto(p_id_producto INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_stock INT;
    
    -- Get product stock
    SELECT stock INTO v_stock
    FROM productos
    WHERE id_producto = p_id_producto;
    
    -- Return -1 when product is not found
    IF v_stock IS NULL THEN
        RETURN -1;
    END IF;
    
    RETURN v_stock;
END //

DELIMITER ;

-- ============================================================================
-- ACTIVITY 4: Procedure usage to update stock
-- ============================================================================

DELIMITER //

CREATE PROCEDURE actualizar_stock(
    IN p_id_producto INT,
    IN p_cantidad_vendida INT
)
BEGIN
    DECLARE v_stock_actual INT;
    DECLARE v_mensaje VARCHAR(200);
    
    -- Get current stock using function
    SET v_stock_actual = obtener_stock_producto(p_id_producto);
    
    -- Check product exists
    IF v_stock_actual = -1 THEN
        SET v_mensaje = CONCAT('ERROR: El producto con ID ', p_id_producto, ' no existe.');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_mensaje;
    END IF;
    
    -- Check sufficient stock
    IF v_stock_actual < p_cantidad_vendida THEN
        SET v_mensaje = CONCAT('ERROR: Stock insuficiente. Stock actual: ', v_stock_actual, 
                              ', Cantidad solicitada: ', p_cantidad_vendida);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_mensaje;
    END IF;
    
    -- Update stock
    UPDATE productos
    SET stock = stock - p_cantidad_vendida
    WHERE id_producto = p_id_producto;
    
    -- Show success message
    SELECT CONCAT('Stock actualizado correctamente. Producto ID: ', p_id_producto,
                 ' | Stock anterior: ', v_stock_actual,
                 ' | Cantidad vendida: ', p_cantidad_vendida,
                 ' | Stock nuevo: ', (v_stock_actual - p_cantidad_vendida)) AS Resultado;
END //

DELIMITER ;

-- ============================================================================
-- ACTIVITY 5: Trigger creation
-- ============================================================================

DELIMITER //

CREATE TRIGGER trg_actualizar_stock
AFTER INSERT ON registro_ventas
FOR EACH ROW
BEGIN
    DECLARE v_stock_actual INT;
    DECLARE v_mensaje VARCHAR(200);
    
    -- Get current stock
    SELECT stock INTO v_stock_actual
    FROM productos
    WHERE id_producto = NEW.id_producto;
    
    -- Check sufficient stock
    IF v_stock_actual < NEW.cantidad THEN
        SET v_mensaje = CONCAT('ERROR: Stock insuficiente para la venta. ',
                              'Producto ID: ', NEW.id_producto,
                              ', Stock disponible: ', v_stock_actual,
                              ', Cantidad solicitada: ', NEW.cantidad);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_mensaje;
    END IF;
    
    -- Update stock
    UPDATE productos
    SET stock = stock - NEW.cantidad
    WHERE id_producto = NEW.id_producto;
END //

DELIMITER ;

-- ============================================================================
-- TESTS AND USAGE EXAMPLES
-- ============================================================================

-- Query initial products
SELECT '=== PRODUCTOS INICIALES ===' AS Info;
SELECT * FROM productos;

-- Query sales log initial state
SELECT '=== REGISTRO DE VENTAS INICIAL ===' AS Info;
SELECT * FROM registro_ventas;

-- Insert sales example. Trigger updates stock automatically
SELECT '=== INSERTANDO VENTAS ===' AS Info;
INSERT INTO registro_ventas (id_producto, cantidad, fecha_venta) VALUES
(1, 10, '2025-11-20'),
(2, 5, '2025-11-21'),
(3, 15, '2025-11-22'),
(1, 8, '2025-11-23');

-- Show stock after sales
SELECT '=== PRODUCTOS DESPUÉS DE VENTAS (Stock actualizado por trigger) ===' AS Info;
SELECT * FROM productos;

-- List all recorded sales
SELECT '=== REGISTRO DE VENTAS ===' AS Info;
SELECT rv.id_venta, p.nombre, rv.cantidad, p.precio, 
       (rv.cantidad * p.precio) AS subtotal, rv.fecha_venta
FROM registro_ventas rv
INNER JOIN productos p ON rv.id_producto = p.id_producto;

-- Calculate total sales using cursor
SELECT '=== CALCULAR TOTAL DE VENTAS (Usando Cursor) ===' AS Info;
CALL calcular_total_ventas();

-- Test obtener_stock_producto function
SELECT '=== CONSULTAR STOCK DE PRODUCTOS (Usando Función) ===' AS Info;
SELECT id_producto, nombre, obtener_stock_producto(id_producto) AS stock_actual
FROM productos;

-- Example usage of actualizar_stock procedure manually
-- Note this case uses the trigger. The procedure is also available
SELECT '=== EJEMPLO DE PROCEDIMIENTO ACTUALIZAR_STOCK ===' AS Info;
-- CALL actualizar_stock(4, 5);  -- Uncomment to test manually

-- Final summary
SELECT '=== RESUMEN FINAL ===' AS Info;
SELECT 
    (SELECT COUNT(*) FROM productos) AS Total_Productos,
    (SELECT COUNT(*) FROM registro_ventas) AS Total_Ventas,
    (SELECT SUM(rv.cantidad * p.precio) FROM registro_ventas rv 
     INNER JOIN productos p ON rv.id_producto = p.id_producto) AS Total_Ingresos;
