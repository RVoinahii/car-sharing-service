package com.carshare.rentalsystem.notifications.telegram.notification.sender.templates;

import com.carshare.rentalsystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationNotificationTemplates {

    public String createAuthenticationSuccessMessage(String telegramName, User user) {
        return String.format(
                """
                        ✅ You have successfully authenticated, %s!
                        
                        👤 Customer:
                        %s
                        """,
                telegramName,
                formatUserInfo(user)
        );
    }

    private String formatUserInfo(User user) {
        return String.format("""
                               ID: %s
                               Name: %s %s
                               Email: %s
                               Role: %s
                            """,
                user.getId(),
                user.getFirstName(), user.getLastName(),
                user.getEmail(),
                user.getRole().getRole().name());
    }
}
