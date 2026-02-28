package com.juliy.store.backend.security.config;

import com.juliy.store.backend.security.domain.AppUser;
import com.juliy.store.backend.security.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultUsersInitializer {

    @Bean
    CommandLineRunner seedDefaultUsers(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        return args -> {
            ensureSchema(jdbcTemplate);
            upsertUser(appUserRepository, passwordEncoder, "admin", "admin123", "ADMIN");
            upsertUser(appUserRepository, passwordEncoder, "cajero", "cajero123", "CAJERO");
        };
    }

    private void ensureSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS productos (
                    id_producto INT PRIMARY KEY AUTO_INCREMENT,
                    nombre VARCHAR(100) NOT NULL,
                    precio DECIMAL(10,2) NOT NULL,
                    stock INT NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS registro_ventas (
                    id_venta INT PRIMARY KEY AUTO_INCREMENT,
                    id_producto INT NOT NULL,
                    cantidad INT NOT NULL,
                    fecha_venta DATE NOT NULL,
                    creado_por VARCHAR(50) NOT NULL DEFAULT 'seed',
                    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    CONSTRAINT fk_registro_ventas_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(120) NOT NULL,
                    role VARCHAR(30) NOT NULL,
                    enabled BOOLEAN NOT NULL DEFAULT TRUE,
                    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
                    last_connection_at TIMESTAMP NULL
                )
                """);

        if (!columnExists(jdbcTemplate, "registro_ventas", "creado_por")) {
            jdbcTemplate.execute("ALTER TABLE registro_ventas ADD COLUMN creado_por VARCHAR(50) NOT NULL DEFAULT 'seed'");
        }

        if (!columnExists(jdbcTemplate, "registro_ventas", "creado_en")) {
            jdbcTemplate.execute("ALTER TABLE registro_ventas ADD COLUMN creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }

        if (!columnExists(jdbcTemplate, "registro_ventas", "actualizado_en")) {
            jdbcTemplate.execute("ALTER TABLE registro_ventas ADD COLUMN actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        }

        if (!columnExists(jdbcTemplate, "usuarios", "must_change_password")) {
            jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE");
        }

        if (!columnExists(jdbcTemplate, "usuarios", "last_connection_at")) {
            jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN last_connection_at TIMESTAMP NULL");
        }

        Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM productos", Integer.class);
        if (productCount == null || productCount == 0) {
            jdbcTemplate.update("INSERT INTO productos (nombre, precio, stock) VALUES (?, ?, ?)", "Producto A", 50.00, 100);
            jdbcTemplate.update("INSERT INTO productos (nombre, precio, stock) VALUES (?, ?, ?)", "Producto B", 75.00, 80);
            jdbcTemplate.update("INSERT INTO productos (nombre, precio, stock) VALUES (?, ?, ?)", "Producto C", 60.00, 120);
            jdbcTemplate.update("INSERT INTO productos (nombre, precio, stock) VALUES (?, ?, ?)", "Producto D", 90.00, 60);
        }
    }

    private boolean columnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private void upsertUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String rawPassword,
            String role
    ) {
        AppUser user = appUserRepository.findById(username).orElseGet(AppUser::new);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        user.setMustChangePassword(false);
        appUserRepository.save(user);
    }
}
