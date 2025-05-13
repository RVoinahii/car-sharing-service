package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.model.Payment;

public record PaymentSuccessEventDto(Payment payment, Long userId) {
}
