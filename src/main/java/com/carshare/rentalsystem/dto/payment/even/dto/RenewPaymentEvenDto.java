package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.model.Payment;

public record RenewPaymentEvenDto(Payment payment, Long userId) {
}
