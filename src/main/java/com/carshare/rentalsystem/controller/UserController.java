package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.client.telegram.TelegramAuthenticationService;
import com.carshare.rentalsystem.dto.telegram.TelegramTokenResponseDto;
import com.carshare.rentalsystem.dto.user.request.dto.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
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

@Tag(
        name = "User management",
        description = """
        Endpoints for managing user accounts and profiles.

        - **Customers and Managers** can:
          - View and update their own profile
          - Link their Telegram account via a secure temporary link
        - **Managers** can:
          - Promote or demote other users by changing their roles (e.g. CUSTOMER ↔ MANAGER)

        Email uniqueness and security are enforced during updates and role changes.
            """
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final TelegramAuthenticationService telegramAuthenticationService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = """
        Returns the profile information of the currently authenticated user, including ID, email,
        full name, and role.
        
        **Required roles:** CUSTOMER, MANAGER
            """
    )
    public UserResponseDto getProfileInfo(Authentication authentication) {
        return userService.getUserInfo(getAuthenticatedUserId(authentication));
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/telegram")
    @Operation(
            summary = "Generate Telegram authentication link",
            description = """
        Generates a one-time Telegram authentication link (valid for 10 minutes) for the currently
        authenticated user. Used to securely link the user's Telegram account.
        
        **Required roles:** CUSTOMER, MANAGER
            """
    )
    public TelegramTokenResponseDto loginToTelegram(Authentication authentication) {
        return telegramAuthenticationService.getTelegramLink(
                getAuthenticatedUserId(authentication));
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PatchMapping("/me")
    @Operation(
            summary = "Update user profile",
            description = """
        Updates the currently authenticated user's profile details (first name, last name,
        and/or email).
        
        Email must be unique in the system. Only non-null fields in the request will be updated.
        
        **Required roles:** CUSTOMER, MANAGER
            """
    )
    public UserResponseDto updateProfileInfo(Authentication authentication,
            @RequestBody @Valid UserUpdateRequestDto updateRequestDto) {
        return userService.updateUserInfo(getAuthenticatedUserId(authentication), updateRequestDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{userId}/role")
    @Operation(
            summary = "Change user role by ID",
            description = """
        Updates the role of a user by their user ID. Allowed roles: CUSTOMER, MANAGER.
        
        **Required roles:** MANAGER
            """
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
