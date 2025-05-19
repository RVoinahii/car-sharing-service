package com.carshare.rentalsystem.dto.payment.even.dto;

import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;

public record PaymentSuccessEventDto(PaymentResponseDto payment, Long userId) {
}
