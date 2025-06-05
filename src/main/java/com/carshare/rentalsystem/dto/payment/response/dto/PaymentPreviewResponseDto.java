package com.carshare.rentalsystem.dto.payment.response.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentPreviewResponseDto {
    private Long id;
    private String status;
    private Long userId;
    private Long rentalId;
    private BigDecimal amountToPay;
}
