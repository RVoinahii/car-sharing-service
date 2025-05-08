package com.carshare.rentalsystem.notifications;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Rental;

public interface NotificationSender {

    void notifyManagersAboutNewRental(Rental rental);

    void notifyCustomerAboutNewRental(Rental rental, Long userId);

    void notifyManagersAboutRentalReturn(Rental rental);

    void notifyCustomerAboutRentalReturn(Rental rental, Long userId);

    void notifyManagersAboutSuccessfulPayment(Payment payment);

    void notifyCustomerAboutSuccessfulPayment(Payment payment, Long userId);

    void notifyManagersAboutPaymentCancel(Payment payment);

    void notifyCustomerAboutPaymentCancel(Payment payment, Long userId);

    void notifyManagersAboutRenewPayment(Payment payment);

    void notifyCustomerAboutRenewPayment(Payment payment, Long userId);
}
