# Changelog

All notable changes to this project are documented in this file.

The format is based on Keep a Changelog and follows Semantic Versioning.

## [1.1.0] - 2026-02-28

### Added
- User management module with admin endpoints to create, enable/disable, reset temporary password, and delete users.
- First-access flow (`POST /api/auth/first-access`) requiring temporary-password replacement.
- User connectivity tracking with `last_connection_at`, plus summary/status endpoints.
- Product creation endpoint and CSV bulk-import endpoint for products.
- Angular account tab with users table, connected users counter, admin actions, and delete confirmation modal.
- Angular dashboard charts using Chart.js.
- Docker support (`docker-compose.yml`, backend/frontend Dockerfiles, `.env.example`, Nginx config).
- GitHub Actions CI workflow (`.github/workflows/ci.yml`) and separate Deploy workflow (`.github/workflows/deploy.yml`).

### Changed
- Backend datasource and CORS properties are now environment-driven for local and containerized runs.
- README updated with Docker, CI/CD, deploy pipeline, new endpoints, and current architecture details.
- Frontend reorganized in tabs (`Dashboard`, `Ventas`, `Productos`, `Cuenta`) for cleaner UX.

## [1.0.0] - 2026-02-27

### Added
- Backend unit tests for JWT token flow and metrics service mapping.
- Deployment checklist in `DEPLOYMENT_CHECKLIST.md`.
- Local startup scripts: `scripts/start-dev.ps1` and `scripts/start-dev.bat`.

### Changed
- Backend Java version normalized to 17 in `backend/pom.xml`.

## [0.9.0] - 2026-02-27

### Added
- CSV export endpoint for sales: `GET /api/sales/export`.
- CSV export endpoint for metrics: `GET /api/metrics/export`.
- Angular dashboard buttons to download sales and metrics reports in CSV format.

## [0.8.0] - 2026-02-27

### Added
- Business metrics endpoint `GET /api/metrics/dashboard`.
- Monthly summary metrics: total revenue, total sales, total units sold.
- Last 7 days sales trend and top 5 products of current month.
- Angular metrics dashboard with cards and metric tables.

## [0.7.0] - 2026-02-27

### Added
- Sales audit fields: creator (`creado_por`), created timestamp (`creado_en`), updated timestamp (`actualizado_en`).
- New activity endpoint `GET /api/sales/activity` for recent sales actions.
- Frontend "Actividad reciente" section showing user and creation timestamp.

### Security
- Sales now persist the authenticated username that created the transaction.

## [0.6.0] - 2026-02-27

### Added
- Database-backed authentication users table (`usuarios`) in SQL setup.
- Custom `UserDetailsService` loading users from MySQL.

### Security
- Endpoint authorization refined by method and resource.
- Product write operations restricted to `ADMIN`.
- Sales read/write available to `ADMIN` and `CAJERO`.

## [0.5.0] - 2026-02-27

### Added
- Refresh token endpoint `POST /api/auth/refresh`.
- Access/refresh token expiration metadata in auth responses.
- Frontend automatic session renewal before token expiration.
- Session countdown timer in Angular UI.

### Security
- Distinct access and refresh token handling.
- Improved JWT filter resilience for invalid tokens.

## [0.4.0] - 2026-02-26

### Added
- Spring Security + JWT authentication in backend.
- Login endpoint `POST /api/auth/login` with roles `ADMIN` and `CAJERO`.
- JWT filter and stateless API security rules.
- Angular login/logout flow with token storage and auth interceptor.

### Security
- Protected products and sales endpoints behind role-based authentication.

## [0.3.0] - 2026-02-26

### Added
- Sales backend module with endpoints `GET /api/sales` and `POST /api/sales`.
- Backend sales validations for product existence and stock availability.
- Angular sales dashboard with sale registration form and sales history table.
- Frontend services and models for sales integration.

## [0.2.0] - 2026-02-26

### Added
- Spring Boot backend in `backend/` with Maven Wrapper.
- Products API (`GET /api/products` and `GET /api/products/{id}`).
- Angular frontend in `frontend/` connected to backend products API.
- Base CORS and MySQL configuration for local backend/frontend integration.

## [0.1.0] - 2026-02-26

### Added
- Initial versioned release baseline.
- Java Swing + MySQL product store management system.
- SQL script with procedures, function, cursor, and trigger integration.