package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.telegram.TelegramTokenResponseDto;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.notifications.telegram.TelegramAuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Telegram bot management", description = "Endpoints for managing telegram")
@RequiredArgsConstructor
@RestController
@RequestMapping("/telegram")
public class TelegramBotController {
    private final TelegramAuthenticationService telegramAuthenticationService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping
    public TelegramTokenResponseDto loginToTelegram(Authentication authentication) {
        return telegramAuthenticationService.getTelegramLink(
                getAuthenticatedUserId(authentication));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
