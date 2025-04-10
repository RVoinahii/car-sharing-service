package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.user.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.UserResponseDto;
import com.carshare.rentalsystem.dto.user.UserUpdateRequestDto;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile info",
            description = "Retrieve the profile information of the currently authenticated user."
                    + "(Required roles: CUSTOMER, MANAGER)"
    )
    public UserResponseDto getProfileInfo(Authentication authentication) {
        return userService.getUserInfo(getAuthenticatedUserId(authentication));
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PatchMapping("/me")
    @Operation(
            summary = "Update current user profile info",
            description = "Update the profile information of the currently authenticated user, "
                    + "such as first name, last name and email."
                    + "(Required roles: CUSTOMER, MANAGER)"
    )
    public UserResponseDto updateProfileInfo(Authentication authentication,
            @RequestBody @Valid UserUpdateRequestDto updateRequestDto) {
        return userService.updateUserInfo(getAuthenticatedUserId(authentication), updateRequestDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{userId}/role")
    @Operation(
            summary = "Update user's role by user ID",
            description = "Update the role of a user by the given user ID."
                    + "(Required roles: MANAGER)"
    )
    public UserResponseDto updateUserRole(@PathVariable Long userId,
            @RequestBody @Valid UpdateUserRoleRequestDto updateRequestDto) {
        return userService.updateUserRole(userId, updateRequestDto);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
