package com.carshare.rentalsystem.service.notifications.telegram.event.listener;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalCreatedEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalReturnEventDto;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalNotificationListener {
    private final NotificationSender rentalNotificationSender;

    @EventListener
    public void handleRentalCreated(RentalCreatedEventDto event) {
        rentalNotificationSender.sendNotification(
                NotificationType.NEW_RENTAL, event.rental(), event.userId()
        );
    }

    @EventListener
    public void handleRentalReturn(RentalReturnEventDto event) {
        rentalNotificationSender.sendNotification(
                NotificationType.RETURN_RENTAL, event.rental(), event.userId()
        );
    }
}
