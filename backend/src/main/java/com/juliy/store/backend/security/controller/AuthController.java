package com.juliy.store.backend.security.controller;

import com.juliy.store.backend.security.dto.AuthRequest;
import com.juliy.store.backend.security.dto.AuthResponse;
import com.juliy.store.backend.security.dto.RefreshTokenRequest;
import com.juliy.store.backend.security.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    public AuthController(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildAuthResponse(userDetails);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isRefreshTokenValid(request.refreshToken(), userDetails)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

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