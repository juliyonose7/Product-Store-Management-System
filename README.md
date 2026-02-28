# Product Store Management System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)
[![CI-CD](https://github.com/juliyonose7/Product-Store-Management-System/actions/workflows/ci.yml/badge.svg)](https://github.com/juliyonose7/Product-Store-Management-System/actions/workflows/ci.yml)
[![Deploy](https://github.com/juliyonose7/Product-Store-Management-System/actions/workflows/deploy.yml/badge.svg)](https://github.com/juliyonose7/Product-Store-Management-System/actions/workflows/deploy.yml)

## Versioning

This project uses [Semantic Versioning](https://semver.org/) with tags in the format `vX.Y.Z`.

- Current version: `1.0.0`
- Current version: `1.1.0`
- Changelog: `CHANGELOG.md`

### How to publish a new version

```bash
# 1) Update VERSION and CHANGELOG.md

# 2) Commit changes
git add VERSION CHANGELOG.md README.md
git commit -m "chore(release): v1.1.0"

# 3) Create and push tag
git tag v1.1.0
git push origin main --tags
```

## Quick start (script)

Use local startup script from repository root:

```bash
scripts/start-dev.bat
```

This opens backend and frontend in separate terminals.

## Docker (recommended for local full stack)

This repository includes Docker support for:
- `mysql` (MySQL 8)
- `backend` (Spring Boot)
- `frontend` (Angular built and served by Nginx)

### 1) Prepare environment

```bash
cp .env.example .env
```

### 2) Build and run all services

```bash
docker compose up --build -d
```

### 3) Access services

- Frontend: `http://localhost:4200`
- Backend health: `http://localhost:8080/actuator/health`

### 4) Stop services

```bash
docker compose down
```

To reset database volume too:

```bash
docker compose down -v
```

## Deployment checklist

See `DEPLOYMENT_CHECKLIST.md` before production deployment.

## CI/CD

GitHub Actions workflow is configured in [.github/workflows/ci.yml](.github/workflows/ci.yml).

It runs on every push and pull request to `main` and executes:
- Backend build (`./mvnw -DskipTests clean package`)
- Frontend build (`npm ci && npm run build`)
- Docker validation (`docker compose config --quiet`)
- Docker image build for backend and frontend

Deployment pipeline is separated in [.github/workflows/deploy.yml](.github/workflows/deploy.yml).

Deploy workflow:
- Runs automatically after CI succeeds on `main` (via `workflow_run`)
- Can be executed manually (`workflow_dispatch`)
- Builds and publishes Docker images to GHCR for backend and frontend

## Current architecture (in migration)

- `TiendaProductosGUI.java`: legacy Swing client.
- `backend/`: Spring Boot REST API.
- `frontend/`: Angular frontend.
- Migration approach: move business logic from Swing + direct SQL into backend services.

## Description

Store management system with Spring Boot + Angular + MySQL. The legacy Swing client is still included for reference while the new platform provides JWT auth, role-based access, dashboard metrics, user administration, CSV imports/exports, Docker, and CI/CD.

## Key features

- JWT authentication with refresh token and role-based permissions (`ADMIN` / `CAJERO`)
- First-access flow requiring password update for temporary credentials
- Account area with user stats, connected users, last connection, and admin controls
- Admin actions: create users, activate/deactivate, delete, and reset temporary passwords
- Product management by form and bulk CSV import
- Sales registration, recent activity, and CSV exports for sales/metrics
- Dashboard metrics with Chart.js (daily vs monthly comparison + top products)
- Docker Compose full stack (`mysql`, `backend`, `frontend`) and GitHub Actions CI/CD + Deploy

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

Set your MySQL password in an environment variable before running:

```powershell
$env:DB_PASSWORD="tu_password_real"
```

Then keep `backend/src/main/resources/application.properties` like this:

```properties
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:tu_contrasena}
```

### 2. Run backend

```bash
backend/mvnw spring-boot:run
```

Backend URL: `http://localhost:8080`

Authentication endpoint:
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/first-access`

Products endpoints:
- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `POST /api/products/import-csv`

Sales endpoints:
- `GET /api/sales`
- `POST /api/sales`
- `GET /api/sales/activity`
- `GET /api/sales/export`

Metrics endpoint:
- `GET /api/metrics/dashboard`
- `GET /api/metrics/export`

Users endpoints:
- `GET /api/users/count`
- `GET /api/users/status`
- `POST /api/users` (admin)
- `PATCH /api/users/{username}/enabled` (admin)
- `POST /api/users/{username}/reset-password` (admin)
- `DELETE /api/users/{username}` (admin)
- `POST /api/users/me/password`

Demo users:
- `admin` / `admin123` (role `ADMIN`)
- `cajero` / `cajero123` (role `CAJERO`)

These users are now loaded from MySQL table `usuarios`.

Authorization rules (finer-grained):
- `GET /api/products/**`: `ADMIN`, `CAJERO`
- `POST|PUT|DELETE /api/products/**`: `ADMIN`
- `GET|POST /api/sales/**`: `ADMIN`, `CAJERO`
- `GET /api/users/count|status`: `ADMIN`, `CAJERO`
- `POST /api/users/me/password`: `ADMIN`, `CAJERO`
- Other `/api/users/**`: `ADMIN`

Sales audit:
- Sales store `creado_por`, `creado_en`, and `actualizado_en`.
- `creado_por` is the authenticated username from JWT.

## Frontend (Angular)

```bash
npm --prefix frontend install
npm --prefix frontend start
```

Frontend URL: `http://localhost:4200`

## Basic tests

Backend unit tests include:
- JWT generation/validation flow.
- Metrics service mapping logic.

Current frontend features:
- Login and logout with JWT.
- Automatic session refresh.
- Session countdown (time remaining).
- Metrics dashboard (summary, trend, top products).
- CSV export buttons for sales and metrics reports.
- Product listing with live stock.
- Sale registration form.
- Sales history with subtotal calculation.
- Recent activity panel with user + timestamp.

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




