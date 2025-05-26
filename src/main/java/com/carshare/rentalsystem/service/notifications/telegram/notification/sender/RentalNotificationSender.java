package com.carshare.rentalsystem.service.notifications.telegram.notification.sender;

import com.carshare.rentalsystem.client.telegram.TelegramBotService;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalNotificationSender implements NotificationSender<RentalResponseDto> {
    private static final Set<NotificationType> DUAL_RECIPIENT_TYPES = Set.of(
            NotificationType.RENTAL_NEW_NOTIF,
            NotificationType.RENTAL_RETURN_NOTIF
    );

    private static final Set<NotificationType> SINGLE_RECIPIENT_TYPES = Set.of(
            NotificationType.RENTAL_DUE_SOON_NOTIF,
            NotificationType.RENTAL_OVERDUE_NOTIF,
            NotificationType.RENTAL_START_NOTIF
    );

    private final MessageTemplateDispatcher messageDispatcher;
    private final TelegramBotService telegramBotService;

    @Async
    @Override
    public void sendNotification(NotificationType type, RentalResponseDto notificationData,
                                 Long userId) {
        EnumMap<MessageRecipient, String> messages = new EnumMap<>(MessageRecipient.class);

        if (DUAL_RECIPIENT_TYPES.contains(type)) {
            for (MessageRecipient recipient : List.of(MessageRecipient.RECIPIENT_CUSTOMER,
                    MessageRecipient.RECIPIENT_MANAGER)) {
                MessageType messageType = MessageType.mapNotificationToMessageType(type);
                String message = messageDispatcher.createMessage(
                        messageType,
                        recipient,
                        notificationData);
                messages.put(recipient, message);
            }

            telegramBotService.notifyRecipients(messages, userId);
        } else if (SINGLE_RECIPIENT_TYPES.contains(type)) {
            MessageType messageType = MessageType.mapNotificationToMessageType(type);
            String message = messageDispatcher.createMessage(
                    messageType,
                    MessageRecipient.RECIPIENT_CUSTOMER,
                    notificationData);

            telegramBotService.notifyCustomer(message, userId);
        } else {
            throw new IllegalArgumentException("Unsupported notification type: " + type);
        }
    }
}
