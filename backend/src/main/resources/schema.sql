CREATE TABLE IF NOT EXISTS productos (
    id_producto INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL
);

CREATE TABLE IF NOT EXISTS registro_ventas (
    id_venta INT PRIMARY KEY AUTO_INCREMENT,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    fecha_venta DATE NOT NULL,
    creado_por VARCHAR(50) NOT NULL DEFAULT 'seed',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_registro_ventas_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(120) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    last_connection_at TIMESTAMP NULL
);

INSERT INTO productos (nombre, precio, stock)
SELECT 'Producto A', 50.00, 100
WHERE NOT EXISTS (SELECT 1 FROM productos);

INSERT INTO productos (nombre, precio, stock)
SELECT 'Producto B', 75.00, 80
WHERE (SELECT COUNT(*) FROM productos) = 1;

INSERT INTO productos (nombre, precio, stock)
SELECT 'Producto C', 60.00, 120
WHERE (SELECT COUNT(*) FROM productos) = 2;

INSERT INTO productos (nombre, precio, stock)
SELECT 'Producto D', 90.00, 60
WHERE (SELECT COUNT(*) FROM productos) = 3;
