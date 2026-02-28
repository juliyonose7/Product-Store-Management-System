package com.juliy.store.backend.security.service;

import com.juliy.store.backend.security.domain.AppUser;
import com.juliy.store.backend.security.dto.ChangePasswordRequest;
import com.juliy.store.backend.security.dto.CreateUserRequest;
import com.juliy.store.backend.security.dto.FirstAccessPasswordRequest;
import com.juliy.store.backend.security.dto.UpdateUserEnabledRequest;
import com.juliy.store.backend.security.dto.AdminResetPasswordRequest;
import com.juliy.store.backend.security.dto.UserConnectionStatusResponse;
import com.juliy.store.backend.security.dto.UserResponse;
import com.juliy.store.backend.security.dto.UsersSummaryResponse;
import com.juliy.store.backend.security.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Comparator;
import java.util.List;

@Service
public class UserManagementService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final long accessExpirationMs;

    public UserManagementService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.jwt.expiration-ms}") long accessExpirationMs
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessExpirationMs = accessExpirationMs;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.username().trim();
        if (appUserRepository.findById(username).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        String role = normalizeRole(request.role());

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.temporaryPassword()));
        user.setRole(role);
        user.setEnabled(true);
        user.setMustChangePassword(true);

        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public void completeFirstAccess(FirstAccessPasswordRequest request) {
        AppUser user = appUserRepository.findByUsernameAndEnabledTrue(request.username())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.username()));

        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new IllegalArgumentException("First access password change is not required for this user");
        }

        if (!passwordEncoder.matches(request.temporaryPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid temporary password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        appUserRepository.save(user);
    }

    @Transactional
    public void changeOwnPassword(String username, ChangePasswordRequest request) {
        AppUser user = appUserRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is invalid");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        appUserRepository.save(user);
    }

    @Transactional
    public UserResponse updateUserEnabled(String targetUsername, String actorUsername, UpdateUserEnabledRequest request) {
        validateAdminUserTarget(targetUsername, actorUsername);

        AppUser user = appUserRepository.findById(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + targetUsername));

        user.setEnabled(Boolean.TRUE.equals(request.enabled()));
        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public void deleteUser(String targetUsername, String actorUsername) {
        validateAdminUserTarget(targetUsername, actorUsername);

        if (!appUserRepository.existsById(targetUsername)) {
            throw new EntityNotFoundException("User not found: " + targetUsername);
        }

        appUserRepository.deleteById(targetUsername);
    }

    @Transactional
    public UserResponse resetUserPassword(String targetUsername, String actorUsername, AdminResetPasswordRequest request) {
        validateAdminUserTarget(targetUsername, actorUsername);

        AppUser user = appUserRepository.findById(targetUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + targetUsername));

        user.setPassword(passwordEncoder.encode(request.temporaryPassword()));
        user.setMustChangePassword(true);
        return toResponse(appUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public long getTotalUsersRegistered() {
        return appUserRepository.count();
    }

    @Transactional
    public void registerConnection(String username) {
        appUserRepository.findByUsernameAndEnabledTrue(username).ifPresent(user -> {
            user.setLastConnectionAt(LocalDateTime.now());
            appUserRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public UsersSummaryResponse getUsersSummary() {
        List<AppUser> users = appUserRepository.findAll();
        long connectedUsers = users.stream()
                .filter(this::isConnected)
                .count();

        return new UsersSummaryResponse(users.size(), connectedUsers);
    }

    @Transactional(readOnly = true)
    public List<UserConnectionStatusResponse> getUsersConnectionStatus() {
        return appUserRepository.findAll().stream()
                .map(user -> new UserConnectionStatusResponse(
                        user.getUsername(),
                        user.getRole(),
                        Boolean.TRUE.equals(user.getEnabled()),
                        isConnected(user),
                        user.getLastConnectionAt()
                ))
                .sorted(Comparator.comparing(UserConnectionStatusResponse::username))
                .toList();
    }

    private boolean isConnected(AppUser user) {
        if (!Boolean.TRUE.equals(user.getEnabled()) || user.getLastConnectionAt() == null) {
            return false;
        }

        LocalDateTime connectedThreshold = LocalDateTime.now().minusNanos(accessExpirationMs * 1_000_000L);
        return user.getLastConnectionAt().isAfter(connectedThreshold);
    }

    private void validateAdminUserTarget(String targetUsername, String actorUsername) {
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Target username is required");
        }

        if (targetUsername.equalsIgnoreCase(actorUsername)) {
            throw new IllegalArgumentException("No puedes ejecutar esta acción sobre tu propio usuario");
        }
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("ADMIN") && !normalized.equals("CAJERO")) {
            throw new IllegalArgumentException("Role must be ADMIN or CAJERO");
        }
        return normalized;
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getUsername(),
                user.getRole(),
                Boolean.TRUE.equals(user.getEnabled()),
                Boolean.TRUE.equals(user.getMustChangePassword())
        );
    }
}
