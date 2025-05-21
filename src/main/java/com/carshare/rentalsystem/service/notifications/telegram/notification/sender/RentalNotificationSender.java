package com.carshare.rentalsystem.service.notifications.telegram.notification.sender;

import com.carshare.rentalsystem.client.telegram.TelegramBotService;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalNotificationSender implements NotificationSender<RentalResponseDto> {
    private final TelegramBotService telegramBotService;
    private final MessageTemplateDispatcher messageDispatcher;

    @Override
    public void sendNotification(NotificationType type, RentalResponseDto notificationData,
                                 Long userId) {
        EnumMap<MessageRecipient, String> messages = new EnumMap<>(MessageRecipient.class);

        switch (type) {
            case RENTAL_NEW_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.RENTAL_NEW_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );

                String managerMessage = messageDispatcher.createMessage(
                        MessageType.RENTAL_NEW_MSG,
                        MessageRecipient.RECIPIENT_MANAGER,
                        notificationData
                );
                messages.put(MessageRecipient.RECIPIENT_CUSTOMER, customerMessage);
                messages.put(MessageRecipient.RECIPIENT_MANAGER, managerMessage);
            }

            case RENTAL_RETURN_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.RENTAL_RETURN_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );

                String managerMessage = messageDispatcher.createMessage(
                        MessageType.RENTAL_RETURN_MSG,
                        MessageRecipient.RECIPIENT_MANAGER,
                        notificationData
                );
                messages.put(MessageRecipient.RECIPIENT_CUSTOMER, customerMessage);
                messages.put(MessageRecipient.RECIPIENT_MANAGER, managerMessage);
            }

            case RENTAL_DUE_SOON_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.RENTAL_DUE_SOON_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );
                telegramBotService.notifyCustomer(customerMessage, userId);
            }

            case RENTAL_OVERDUE_NOTIF -> {
                String customerMessage = messageDispatcher.createMessage(
                        MessageType.RENTAL_OVERDUE_MSG,
                        MessageRecipient.RECIPIENT_CUSTOMER,
                        notificationData
                );
                telegramBotService.notifyCustomer(customerMessage, userId);
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported notification type: " + type);
        }
        telegramBotService.notifyRecipients(messages, userId);
    }
}
