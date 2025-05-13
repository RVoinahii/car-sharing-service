package com.carshare.rentalsystem.service.notifications.telegram.notification.sender;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import com.carshare.rentalsystem.service.notifications.telegram.TelegramBotService;
import com.carshare.rentalsystem.service.notifications.telegram.templates.NotificationRecipient;
import com.carshare.rentalsystem.service.notifications.telegram.templates.RentalNotificationTemplates;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalNotificationSender implements NotificationSender {
    private final TelegramBotService telegramBotService;
    private final RentalNotificationTemplates rentalNotificationTemplates;

    @Override
    public void sendNotification(NotificationType type, Object notificationData, Long userId) {
        if (!(notificationData instanceof Rental rental)) {
            throw new IllegalArgumentException("Expected Rental as notification data");
        }

        EnumMap<NotificationRecipient, String> messages;

        switch (type) {
            case NEW_RENTAL ->
                    messages = rentalNotificationTemplates.createNewRentalMessages(rental);

            case RETURN_RENTAL ->
                    messages = rentalNotificationTemplates.createRentalReturnMessages(rental);

            default -> throw new IllegalArgumentException(
                    "Unsupported notification type: " + type);
        }

        telegramBotService.notifyRecipients(messages, userId);
    }
}
