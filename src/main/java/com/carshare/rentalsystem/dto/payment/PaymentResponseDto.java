package com.carshare.rentalsystem.dto.payment;

import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.model.Payment.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentResponseDto {
    private Long id;
    private PaymentStatus status;
    private RentalResponseDto rental;
    private String sessionId;
    private String sessionUrl;
    private BigDecimal amountToPay;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredAt;
}
