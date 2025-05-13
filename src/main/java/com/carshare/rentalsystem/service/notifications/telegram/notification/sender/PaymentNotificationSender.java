package com.carshare.rentalsystem.service.notifications.telegram.notification.sender;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import com.carshare.rentalsystem.service.notifications.telegram.TelegramBotService;
import com.carshare.rentalsystem.service.notifications.telegram.templates.NotificationRecipient;
import com.carshare.rentalsystem.service.notifications.telegram.templates.PaymentNotificationTemplates;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentNotificationSender implements NotificationSender {
    private final TelegramBotService telegramBotService;
    private final PaymentNotificationTemplates paymentNotificationTemplates;

    @Override
    public void sendNotification(NotificationType type, Object notificationData, Long userId) {
        if (!(notificationData instanceof Payment payment)) {
            throw new IllegalArgumentException("Expected Rental as notification data");
        }

        EnumMap<NotificationRecipient, String> messages;

        switch (type) {
            case SUCCESSFUL_PAYMENT ->
                    messages = paymentNotificationTemplates
                            .createSuccessfulPaymentMessages(payment);

            case PAYMENT_CANCEL ->
                    messages = paymentNotificationTemplates.createPaymentCancelMessages(payment);

            case RENEW_PAYMENT ->
                    messages = paymentNotificationTemplates.createRenewPaymentMessage(payment);

            default -> throw new IllegalArgumentException(
                    "Unsupported notification type: " + type);
        }

        telegramBotService.notifyRecipients(messages, userId);
    }
}
