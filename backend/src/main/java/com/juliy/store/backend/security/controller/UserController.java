package com.juliy.store.backend.security.controller;

import com.juliy.store.backend.security.dto.ChangePasswordRequest;
import com.juliy.store.backend.security.dto.CreateUserRequest;
import com.juliy.store.backend.security.dto.UpdateUserEnabledRequest;
import com.juliy.store.backend.security.dto.AdminResetPasswordRequest;
import com.juliy.store.backend.security.dto.UserConnectionStatusResponse;
import com.juliy.store.backend.security.dto.UserResponse;
import com.juliy.store.backend.security.dto.UsersSummaryResponse;
import com.juliy.store.backend.security.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userManagementService.createUser(request);
    }

    @GetMapping("/count")
    public UsersSummaryResponse getUsersCount() {
        return userManagementService.getUsersSummary();
    }

    @GetMapping("/status")
    public List<UserConnectionStatusResponse> getUsersStatus() {
        return userManagementService.getUsersConnectionStatus();
    }

    @PatchMapping("/{username}/enabled")
    public UserResponse updateUserEnabled(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserEnabledRequest request,
            Authentication authentication
    ) {
        return userManagementService.updateUserEnabled(username, authentication.getName(), request);
    }

    @PostMapping("/{username}/reset-password")
    public UserResponse resetUserPassword(
            @PathVariable String username,
            @Valid @RequestBody AdminResetPasswordRequest request,
            Authentication authentication
    ) {
        return userManagementService.resetUserPassword(username, authentication.getName(), request);
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String username, Authentication authentication) {
        userManagementService.deleteUser(username, authentication.getName());
    }

    @PostMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeOwnPassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        userManagementService.changeOwnPassword(authentication.getName(), request);
    }
}
