package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;

public record RenewPaymentEvenDto(PaymentResponseDto payment, Long userId) {
}
