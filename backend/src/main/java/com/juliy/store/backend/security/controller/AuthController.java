package com.juliy.store.backend.security.controller;

import com.juliy.store.backend.security.dto.AuthRequest;
import com.juliy.store.backend.security.dto.AuthResponse;
import com.juliy.store.backend.security.dto.FirstAccessPasswordRequest;
import com.juliy.store.backend.security.dto.RefreshTokenRequest;
import com.juliy.store.backend.security.repository.AppUserRepository;
import com.juliy.store.backend.security.service.JwtService;
import com.juliy.store.backend.security.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final UserManagementService userManagementService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService,
            AppUserRepository appUserRepository,
            UserManagementService userManagementService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.userManagementService = userManagementService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        boolean mustChangePassword = appUserRepository.findByUsernameAndEnabledTrue(request.username())
                .map(user -> Boolean.TRUE.equals(user.getMustChangePassword()))
                .orElse(false);

        if (mustChangePassword) {
            throw new IllegalStateException("FIRST_ACCESS_PASSWORD_CHANGE_REQUIRED");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        userManagementService.registerConnection(userDetails.getUsername());
        return buildAuthResponse(userDetails);
    }

    @PostMapping("/first-access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeFirstAccess(@Valid @RequestBody FirstAccessPasswordRequest request) {
        userManagementService.completeFirstAccess(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isRefreshTokenValid(request.refreshToken(), userDetails)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        userManagementService.registerConnection(userDetails.getUsername());
        return buildAuthResponse(userDetails);
    }

    private AuthResponse buildAuthResponse(UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        String role = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                userDetails.getUsername(),
                role,
                jwtService.extractExpirationEpochMs(accessToken),
                jwtService.extractExpirationEpochMs(refreshToken)
        );
    }
}