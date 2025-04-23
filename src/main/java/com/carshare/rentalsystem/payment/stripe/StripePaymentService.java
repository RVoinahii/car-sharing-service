package com.carshare.rentalsystem.payment.stripe;

import com.carshare.rentalsystem.dto.payment.CreatePaymentRequestDto;
import com.carshare.rentalsystem.dto.payment.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StripePaymentService {

    Page<PaymentResponseDto> getAllPayments(Long userId, Pageable pageable);

    PaymentPreviewResponseDto createStripeSession(CreatePaymentRequestDto requestDto, Long userId);

    PaymentPreviewResponseDto renewStripeSession(Long userId, Long paymentId);

    PaymentResponseDto handleSuccess(String sessionId);

    String handleCancel();
}
