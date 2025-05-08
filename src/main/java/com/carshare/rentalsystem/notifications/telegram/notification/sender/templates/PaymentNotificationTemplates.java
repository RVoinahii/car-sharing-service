package com.carshare.rentalsystem.notifications.telegram.notification.sender.templates;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.notifications.NotificationType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentNotificationTemplates {
    public String createSuccessfulPaymentMessage(NotificationType type, Payment payment) {
        Rental rental = payment.getRental();
        User user = rental.getUser();
        Car car = rental.getCar();

        if (type == NotificationType.CUSTOMER) {
            return String.format(
                    """
                            👋 Hello, %s!
                            
                            ✅ Payment completed successfully!
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
            
                            🗓 Rental Period:
                               From: %s
                               To: %s
            
                            💳 Payment:
                               Payment ID: %s
                               Rental ID: %s
                               Amount Paid: %.2f USD
                               Type: %s
                               Status: %s
            
                            ⏳ Paid At: %s
                            """,
                    user.getFirstName(),
                    formatUserInfo(user),
                    formatCarInfo(car),
                    rental.getRentalDate(),
                    rental.getActualReturnDate(),
                    payment.getId(),
                    rental.getId(),
                    payment.getAmountToPay(),
                    payment.getType(),
                    payment.getStatus(),
                    LocalDateTime.now()
            );
        } else {
            return String.format(
                    """
                            ✅ Payment completed successfully!
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            💵 Paid At: %s
                            ⏳ Expires At: %s
                            """,
                    formatUserInfo(user),
                    formatCarInfo(car),
                    formatRentalInfo(rental),
                    formatFullPaymentInfo(payment),
                    payment.getCreatedAt(),
                    LocalDateTime.now(),
                    payment.getExpiredAt()
            );
        }
    }

    public String createPaymentCancelMessage(NotificationType type, Payment payment) {
        Rental rental = payment.getRental();
        User user = rental.getUser();
        Car car = rental.getCar();

        if (type == NotificationType.CUSTOMER) {
            return String.format(
                    """
                            👋 Hello, %s!
                            ❌ Your payment was cancelled!
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            ⏳ Expires At: %s
                            
                            The session remains accessible for 24 hours after creation.
                            
                            Please contact support if you have any questions.
                            """,
                    user.getFirstName(),
                    formatShortPaymentInfo(payment),
                    payment.getCreatedAt(),
                    payment.getExpiredAt()
            );
        } else {
            return String.format(
                    """
                            ❌ Payment was cancelled!
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            ⏳ Expires At: %s
                            """,
                    formatUserInfo(user),
                    formatCarInfo(car),
                    formatRentalInfo(rental),
                    formatFullPaymentInfo(payment),
                    payment.getCreatedAt(),
                    payment.getExpiredAt()
            );
        }
    }

    public String createRenewPaymentMessage(NotificationType type, Payment payment) {
        Rental rental = payment.getRental();
        User user = payment.getRental().getUser();

        if (type == NotificationType.CUSTOMER) {
            return String.format(
                    """
                            👋 Hello, %s!
                            🔄 Your payment session has been renewed!
                            
                            💳 Payment Info:
                            %s
    
                            📅 Created At: %s
                            ⏳ Expiration Time: %s
    
                            You can now proceed with your payment.
                            Please complete it before the session expires.
                            
                            If you face any issues, please contact support.
                            """,
                    user.getFirstName(),
                    formatShortPaymentInfo(payment),
                    payment.getCreatedAt(),
                    payment.getExpiredAt()
            );
        } else {
            return String.format(
                    """
                            🔄 A payment session has been renewed by the customer.
                            
                            👤 Customer:
                            %s
                            
                            💳 Payment Info:
                            %s
        
                            📅 Created At: %s
                            ⏳ Expiration Time: %s
        
                            The customer has requested to renew the payment session.
                            Please monitor the status in case further assistance is needed.
                            """,
                    formatUserInfo(user),
                    formatShortPaymentInfo(payment),
                    payment.getCreatedAt(),
                    payment.getExpiredAt()
            );
        }
    }

    private String formatUserInfo(User user) {
        return String.format("""
                               ID: %s
                               Name: %s %s
                               Email: %s
                            """,
                user.getId(),
                user.getFirstName(), user.getLastName(),
                user.getEmail());
    }

    private String formatCarInfo(Car car) {
        return String.format("""
                               Brand: %s
                               Model: %s
                               Daily fee: %.2f USD
                            """,
                car.getBrand(),
                car.getModel(),
                car.getDailyFee());
    }

    private String formatRentalInfo(Rental rental) {
        return String.format("""
                               Rental ID: %s
                               Rental Start: %s
                               Rental Expected End: %s
                               Rental Actual End: %s
                            """,
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                rental.getActualReturnDate());
    }

    private String formatFullPaymentInfo(Payment payment) {
        return String.format("""
                               Payment ID: %s
                               Amount Paid: %.2f USD
                               Payment Type: %s
                               Payment Status: %s
                               Session ID: %s
                               Session URL: %s
                            """,
                payment.getId(),
                payment.getAmountToPay(),
                payment.getType(),
                payment.getStatus(),
                payment.getSessionId(),
                payment.getSessionUrl());
    }

    private String formatShortPaymentInfo(Payment payment) {
        return String.format("""
                               Payment ID: %s
                               Rental ID: %s
                               Amount: %.2f USD
                            """,
                payment.getId(),
                payment.getRental().getId(),
                payment.getAmountToPay());
    }
}
