package com.carshare.rentalsystem.dto.payment.request.dto;

import com.carshare.rentalsystem.model.Payment;

public record PaymentSearchParameters(
        String userId,
        Payment.PaymentStatus status) {
}
