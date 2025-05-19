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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication management", description = "Endpoints for managing authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @Operation(
            summary = "Create a new user",
            description = "Create a new user with the provided parameters"
    )
    public UserResponseDto registerUser(
            @RequestBody @Valid UserRegistrationRequestDto userRequestDto) {
        return userService.register(userRequestDto);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate existing user",
            description = "Authenticate existing user with the provided parameters"
    )
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authenticate(requestDto);
    }
}
