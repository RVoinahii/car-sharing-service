package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.user.request.dto.UserLoginRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserLoginResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.security.AuthenticationService;
import com.carshare.rentalsystem.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Authentication management",
        description = """
        Endpoints for user registration and login.

        - Register new users with required personal and credential information
        - Authenticate existing users and return a JWT token for further access

        These endpoints are public and do not require prior authentication.
            """
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    @Operation(
            summary = "Register a new user account",
            description = """
        Creates a new user by providing:
        - Valid email address
        - Password (8–20 characters)
        - Matching repeat password
        - First and last name

        Returns user details upon successful registration.
            """
    )
    public UserResponseDto registerUser(
            @RequestBody @Valid UserRegistrationRequestDto userRequestDto) {
        return userService.register(userRequestDto);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in with existing user credentials",
            description = """
        Authenticates a user using their email and password.
        Returns a JWT access token and basic user information if authentication succeeds.
            """
    )
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
