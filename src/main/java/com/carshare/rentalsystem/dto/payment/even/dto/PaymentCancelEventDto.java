package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;

public record PaymentCancelEventDto(PaymentResponseDto payment, Long userId) {
}
