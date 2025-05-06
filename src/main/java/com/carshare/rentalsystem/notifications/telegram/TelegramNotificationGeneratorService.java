package com.carshare.rentalsystem.notifications.telegram;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.notifications.NotificationGeneratorService;
import com.carshare.rentalsystem.notifications.telegram.notification.templates.PaymentNotificationTemplates;
import com.carshare.rentalsystem.notifications.telegram.notification.templates.RentalNotificationTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramNotificationGeneratorService implements NotificationGeneratorService {
    private final TelegramNotificationsService telegramNotificationsService;
    private final RentalNotificationTemplates rentalNotificationTemplates;
    private final PaymentNotificationTemplates paymentNotificationTemplates;

    @Override
    public void notifyManagersAboutNewRental(Rental rental) {
        String message = rentalNotificationTemplates.createNewRentalMessage(
                NotificationType.MANAGER, rental);
        telegramNotificationsService.notifyManagers(message);
    }

    @Override
    public void notifyCustomerAboutNewRental(Rental rental, Long userId) {
        String message = rentalNotificationTemplates.createNewRentalMessage(
                NotificationType.CUSTOMER, rental);
        telegramNotificationsService.notifyCustomer(message, userId);
    }

    @Override
    public void notifyManagersAboutRentalReturn(Rental rental) {
        String message = rentalNotificationTemplates.createRentalReturnMessage(
                NotificationType.MANAGER, rental);
        telegramNotificationsService.notifyManagers(message);
    }

    @Override
    public void notifyCustomerAboutRentalReturn(Rental rental, Long userId) {
        String message = rentalNotificationTemplates.createRentalReturnMessage(
                NotificationType.CUSTOMER, rental);
        telegramNotificationsService.notifyCustomer(message, userId);
    }

    @Override
    public void notifyManagersAboutSuccessfulPayment(Payment payment) {
        String message = paymentNotificationTemplates.createSuccessfulPaymentMessage(
                NotificationType.MANAGER, payment);
        telegramNotificationsService.notifyManagers(message);
    }

    @Override
    public void notifyCustomerAboutSuccessfulPayment(Payment payment, Long userId) {
        String message = paymentNotificationTemplates.createSuccessfulPaymentMessage(
                NotificationType.CUSTOMER, payment);
        telegramNotificationsService.notifyCustomer(message, userId);
    }

    @Override
    public void notifyManagersAboutPaymentCancel(Payment payment) {
        String message = paymentNotificationTemplates.createPaymentCancelMessage(
                NotificationType.MANAGER, payment);
        telegramNotificationsService.notifyManagers(message);
    }

    @Override
    public void notifyCustomerAboutPaymentCancel(Payment payment, Long userId) {
        String message = paymentNotificationTemplates.createPaymentCancelMessage(
                NotificationType.CUSTOMER, payment);
        telegramNotificationsService.notifyCustomer(message, userId);
    }

    @Override
    public void notifyManagersAboutRenewPayment(Payment payment) {
        String message = paymentNotificationTemplates.createRenewPaymentMessage(
                NotificationType.MANAGER, payment);
        telegramNotificationsService.notifyManagers(message);
    }

    @Override
    public void notifyCustomerAboutRenewPayment(Payment payment, Long userId) {
        String message = paymentNotificationTemplates.createRenewPaymentMessage(
                NotificationType.CUSTOMER, payment);
        telegramNotificationsService.notifyCustomer(message, userId);
    }
}
