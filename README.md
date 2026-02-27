# Product Store Management System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)

## Versioning

This project uses [Semantic Versioning](https://semver.org/) with tags in the format `vX.Y.Z`.

- Current version: `0.6.0`
- Changelog: `CHANGELOG.md`

### How to publish a new version

```bash
# 1) Update VERSION and CHANGELOG.md

# 2) Commit changes
git add VERSION CHANGELOG.md README.md
git commit -m "chore(release): v0.6.0"

# 3) Create and push tag
git tag v0.6.0
git push origin main --tags
```

## Current architecture (in migration)

- `TiendaProductosGUI.java`: legacy Swing client.
- `backend/`: Spring Boot REST API.
- `frontend/`: Angular frontend.
- Migration approach: move business logic from Swing + direct SQL into backend services.

## Description

Store management system that integrates a Java Swing user interface with a MySQL database. The project implements cursors, functions, stored procedures, and triggers to manage products and sales.

## Key features

- Tabbed user interface for navigation
- Product management with listing and stock tracking
- Sales registration with automatic validation
- Sales reporting with total calculation
- SQL functions callable from the UI
- Automatic stock updates using triggers

## Database structure

### Database

`tienda_productos`

### Tables

| Table | Description |
|-------|-------------|
| `productos` | Stores available product data |
| `registro_ventas` | Records all sales transactions |

#### Table `productos`

| Field | Type | Description |
|-------|------|-------------|
| `id_producto` | INT PK AUTO_INCREMENT | Unique identifier |
| `nombre` | VARCHAR(100) | Product name |
| `precio` | DECIMAL(10,2) | Unit price |
| `stock` | INT | Available quantity |

#### Table `registro_ventas`

| Field | Type | Description |
|-------|------|-------------|
| `id_venta` | INT PK AUTO_INCREMENT | Sale identifier |
| `id_producto` | INT FK | Product reference |
| `cantidad` | INT | Quantity sold |
| `fecha_venta` | DATE | Transaction date |

## Implemented SQL components

### Cursor `calcular_total_ventas()`

Procedure that iterates through all sales using a cursor to calculate total revenue.

```sql
CALL calcular_total_ventas();
```

### Function `obtener_stock_producto(id)`

Returns the stock for a specific product. Returns `-1` when the product does not exist.

```sql
SELECT obtener_stock_producto(1);
```

### Procedure `actualizar_stock(id, cantidad)`

Updates product stock by subtracting the sold quantity with existence and availability validation.

```sql
CALL actualizar_stock(1, 10);
```

### Trigger `trg_actualizar_stock`

Executes after a sale insert and updates stock for the related product.

## User interface

The application includes four main tabs.

| Tab | Functionality |
|---------|---------------|
| Products | Displays products with current stock |
| Register sale | Form for registering new sales |
| Sales report | Full history with totals |
| SQL functions | Direct execution of functions and procedures |

## Project structure

```
Product-Store-Management-System
├── TiendaProductosGUI.java         # Legacy Swing client
├── laboratorio_sql.sql             # SQL schema and procedures
├── backend/                        # Spring Boot API
├── frontend/                       # Angular UI
├── VERSION
├── CHANGELOG.md
└── README.md
```

## Prerequisites

- Java JDK 17
- MySQL Server 5.7 or later
- Node.js and npm (for Angular frontend)

## Installation and execution

### 1. Configure the database

```bash
# Run the SQL script in MySQL
mysql -u root -p < laboratorio_sql.sql
```

### 2. Configure credentials

Edit the connection constants in `TiendaProductosGUI.java`.

```java
private static final String URL = "jdbc:mysql://localhost:3306/tienda_productos";
private static final String USER = "root";
private static final String PASSWORD = "tu_contraseña";
```

### 3. Compile and run

```bash
# Compile
javac -cp ".;lib/mysql-connector-j-9.1.0.jar" TiendaProductosGUI.java

# Run
java -cp ".;lib/mysql-connector-j-9.1.0.jar" TiendaProductosGUI
```

## Backend (Spring Boot)

### 1. Configure DB credentials

Update `backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=tu_contrasena
```

### 2. Run backend

```bash
backend/mvnw spring-boot:run
```

Backend URL: `http://localhost:8080`

Authentication endpoint:
- `POST /api/auth/login`
- `POST /api/auth/refresh`

Products endpoints:
- `GET /api/products`
- `GET /api/products/{id}`

Sales endpoints:
- `GET /api/sales`
- `POST /api/sales`

Demo users:
- `admin` / `admin123` (role `ADMIN`)
- `cajero` / `cajero123` (role `CAJERO`)

These users are now loaded from MySQL table `usuarios`.

Authorization rules (finer-grained):
- `GET /api/products/**`: `ADMIN`, `CAJERO`
- `POST|PUT|DELETE /api/products/**`: `ADMIN`
- `GET|POST /api/sales/**`: `ADMIN`, `CAJERO`

## Frontend (Angular)

```bash
npm --prefix frontend install
npm --prefix frontend start
```

Frontend URL: `http://localhost:4200`

Current frontend features:
- Login and logout with JWT.
- Automatic session refresh.
- Session countdown (time remaining).
- Product listing with live stock.
- Sale registration form.
- Sales history with subtotal calculation.

## UI functionality

### Product management

- Table view with ID, name, price, and stock
- Refresh button to update data

### Sales registration

- Product ID and quantity input
- Automatic validation of available stock
- Result area with operation confirmation

### Sales report

- Full history with transaction details
- Automatic subtotal calculation
- Overall sales total

### SQL functions

- Stock lookup by product ID
- Manual stock update
- Cursor execution to calculate totals




