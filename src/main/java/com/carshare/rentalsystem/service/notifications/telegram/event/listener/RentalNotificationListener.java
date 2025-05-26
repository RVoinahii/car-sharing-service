package com.carshare.rentalsystem.service.notifications.telegram.event.listener;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalCreatedEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalDueSoonEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalOverdueEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalReturnEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalStartEventDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalNotificationListener {
    private final NotificationSender<RentalResponseDto> rentalNotificationSender;

    @EventListener
    public void handleRentalCreated(RentalCreatedEventDto event) {
        rentalNotificationSender.sendNotification(
                NotificationType.RENTAL_NEW_NOTIF, event.rental(), event.userId()
        );
    }

    @EventListener
    public void handleRentalReturn(RentalReturnEventDto event) {
        rentalNotificationSender.sendNotification(
                NotificationType.RENTAL_RETURN_NOTIF, event.rental(), event.userId()
        );
    }

    @EventListener
    public void handleRentalStart(RentalStartEventDto eventDto) {
        rentalNotificationSender.sendNotification(
                NotificationType.RENTAL_START_NOTIF, eventDto.rental(), eventDto.userId());
    }

    @EventListener
    public void handleRentalDueSoon(RentalDueSoonEventDto event) {
        rentalNotificationSender.sendNotification(
                NotificationType.RENTAL_DUE_SOON_NOTIF, event.rental(), event.userId()
        );
    }

    @EventListener
    public void handleRentalOverdue(RentalOverdueEventDto event) {
        rentalNotificationSender.sendNotification(
                NotificationType.RENTAL_OVERDUE_NOTIF, event.rental(), event.userId()
        );
    }
}
