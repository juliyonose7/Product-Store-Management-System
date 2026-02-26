# Changelog

All notable changes to this project are documented in this file.

The format is based on Keep a Changelog and follows Semantic Versioning.

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