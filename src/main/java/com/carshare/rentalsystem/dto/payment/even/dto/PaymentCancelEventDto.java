package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.model.Payment;

public record PaymentCancelEventDto(Payment payment, Long userId) {
}
