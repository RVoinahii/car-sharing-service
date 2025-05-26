package com.carshare.rentalsystem.dto.payment.request.dto;

import jakarta.validation.constraints.Positive;

public record CreatePaymentRequestDto(
        @Positive
        Long rentalId
) {
}
