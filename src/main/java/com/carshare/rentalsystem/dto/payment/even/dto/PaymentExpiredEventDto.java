package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;

public record PaymentExpiredEventDto(PaymentResponseDto payment, Long userId) {
}
