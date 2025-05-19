package com.carshare.rentalsystem.service.notifications.telegram.notification.sender;

import com.carshare.rentalsystem.client.telegram.TelegramBotService;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentNotificationSender implements NotificationSender<PaymentResponseDto> {
    private final TelegramBotService telegramBotService;
    private final MessageTemplateDispatcher messageDispatcher;

    @Override
    public void sendNotification(NotificationType type, PaymentResponseDto notificationData,
                                 Long userId) {
        EnumMap<MessageRecipient, String> messages = new EnumMap<>(MessageRecipient.class);

        switch (type) {
            case PAYMENT_SUCCESS_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.PAYMENT_SUCCESS_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );

                String managerMessage = messageDispatcher.createMessage(
                        MessageType.PAYMENT_SUCCESS_MSG,
                        MessageRecipient.RECIPIENT_MANAGER,
                        notificationData
                );
                messages.put(MessageRecipient.RECIPIENT_CUSTOMER, customerMessage);
                messages.put(MessageRecipient.RECIPIENT_MANAGER, managerMessage);
            }

            case PAYMENT_CANCEL_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.PAYMENT_CANCEL_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );

                String managerMessage = messageDispatcher.createMessage(
                        MessageType.PAYMENT_CANCEL_MSG,
                        MessageRecipient.RECIPIENT_MANAGER,
                        notificationData
                );
                messages.put(MessageRecipient.RECIPIENT_CUSTOMER, customerMessage);
                messages.put(MessageRecipient.RECIPIENT_MANAGER, managerMessage);
            }

            case PAYMENT_RENEW_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.PAYMENT_RENEW_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );

                String managerMessage = messageDispatcher.createMessage(
                        MessageType.PAYMENT_SUCCESS_MSG,
                        MessageRecipient.RECIPIENT_MANAGER,
                        notificationData
                );
                messages.put(MessageRecipient.RECIPIENT_CUSTOMER, customerMessage);
                messages.put(MessageRecipient.RECIPIENT_MANAGER, managerMessage);
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported notification type: " + type);
        }
        telegramBotService.notifyRecipients(messages, userId);
    }
}
