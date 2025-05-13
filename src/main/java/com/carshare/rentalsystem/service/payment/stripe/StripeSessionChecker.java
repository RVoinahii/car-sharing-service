package com.carshare.rentalsystem.service.payment.stripe;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentStatus;
import com.carshare.rentalsystem.repository.payment.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripeSessionChecker {
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 3_600_000)
    public void checkExpiredSessions() {
        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        for (Payment payment : payments) {
            if (payment.getExpiredAt().isBefore(LocalDateTime.now())) {
                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
            }
        }
    }
}
