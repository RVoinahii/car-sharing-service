package com.carshare.rentalsystem.service.notifications.telegram.event.listener;

import com.carshare.rentalsystem.dto.payment.even.dto.PaymentCancelEventDto;
import com.carshare.rentalsystem.dto.payment.even.dto.PaymentSuccessEventDto;
import com.carshare.rentalsystem.dto.payment.even.dto.RenewPaymentEvenDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.service.notifications.NotificationSender;
import com.carshare.rentalsystem.service.notifications.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentNotificationListener {
    private final NotificationSender<PaymentResponseDto> paymentNotificationSender;

    @EventListener
    public void handleSuccessfulPayment(PaymentSuccessEventDto event) {
        paymentNotificationSender.sendNotification(
                NotificationType.PAYMENT_SUCCESS_NOTIF, event.payment(), event.userId()
        );
    }

    @EventListener
    public void handlePaymentCancel(PaymentCancelEventDto event) {
        paymentNotificationSender.sendNotification(
                NotificationType.PAYMENT_CANCEL_NOTIF, event.payment(), event.userId()
        );
    }

    @EventListener
    public void handleRenewPayment(RenewPaymentEvenDto event) {
        paymentNotificationSender.sendNotification(
                NotificationType.PAYMENT_RENEW_NOTIF, event.payment(), event.userId()
        );
    }
}
