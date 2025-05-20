package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateBuilder;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;

public abstract class BasePaymentMessageBuilder<T> implements MessageTemplateBuilder<T> {
    protected String formatLitePaymentInfo(PaymentResponseDto payment) {
        return String.format("""
                               Payment ID: %s
                               Rental ID: %s
                               Amount: %.2f USD
                            """,
                payment.getId(),
                payment.getRental().getId(),
                payment.getAmountToPay());
    }

    protected String formatPaymentInfo(PaymentResponseDto payment) {
        return String.format("""
                               Payment ID: %s
                               Amount Paid: %.2f USD
                               Payment Type: %s
                               Payment Status: %s
                               Session ID: %s
                            """,
                payment.getId(),
                payment.getAmountToPay(),
                payment.getType(),
                payment.getStatus(),
                payment.getSessionId());
    }

    protected String formatRentalInfo(RentalPreviewResponseDto rental) {
        return String.format("""
                               Rental ID: %s
                               Rental user ID: %s
                               Rental car ID: %s
                            """,
                rental.getId(),
                rental.getUserId(),
                rental.getCarId());
    }
}
