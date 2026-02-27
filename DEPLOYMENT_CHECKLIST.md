# Deployment Checklist

## 1) Prerequisites

- Java JDK 17 installed.
- Node.js and npm installed.
- MySQL running and reachable.

## 2) Database

- Run `laboratorio_sql.sql` in MySQL.
- Verify tables: `productos`, `registro_ventas`, `usuarios`.
- Verify demo users exist in `usuarios`.

## 3) Backend configuration

- Check `backend/src/main/resources/application.properties`.
- Update `spring.datasource.username` and `spring.datasource.password`.
- Validate JWT settings are present and non-empty.

## 4) Build validation

- Backend compile: `backend/mvnw -f backend/pom.xml -DskipTests compile`
- Frontend build: `npm --prefix frontend run build`

## 5) Runtime validation

- Start backend and frontend.
- Login works with `admin` and `cajero`.
- Sales CRUD flow works.
- Metrics dashboard loads.
- CSV exports download correctly.

## 6) Release hygiene

- Update `VERSION`, `CHANGELOG.md`, and `README.md`.
- Commit release changes.
- Create and push tag (`vX.Y.Z`).
