package com.carshare.rentalsystem.dto.payment.request.dto;

import com.carshare.rentalsystem.model.Payment.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequestDto(
        @Positive
        Long rentalId,

        @NotBlank
        PaymentType paymentType
) {
}
