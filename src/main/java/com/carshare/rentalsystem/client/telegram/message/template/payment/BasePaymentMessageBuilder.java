package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateBuilder;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;

public abstract class BasePaymentMessageBuilder<T> implements MessageTemplateBuilder<T> {
    protected String formatUserInfo(UserPreviewResponseDto user) {
        return String.format("""
                               ID: %s
                               Name: %s
                               Email: %s
                            """,
                user.getId(),
                user.getFullName(),
                user.getEmail());
    }

    protected String formatShortPaymentInfo(PaymentResponseDto payment) {
        return String.format("""
                               Payment ID: %s
                               Rental ID: %s
                               Amount: %.2f USD
                            """,
                payment.getId(),
                payment.getRental().getId(),
                payment.getAmountToPay());
    }

    protected String formatFullPaymentInfo(PaymentResponseDto payment) {
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

    protected String formatCarInfo(CarPreviewResponseDto car) {
        return String.format("""
                               Brand: %s
                               Model: %s
                               Daily fee: %.2f USD
                            """,
                car.getBrand(),
                car.getModel(),
                car.getDailyFee());
    }

    protected String formatRentalInfo(RentalResponseDto rental) {
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
}
