package com.carshare.rentalsystem.dto.payment.response.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentCancelResponseDto {
    private Long id;
    private String sessionId;
    private String sessionUrl;
    private BigDecimal amountToPay;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredAt;
    private String cancelMessage;
}
