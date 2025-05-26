package com.carshare.rentalsystem.service.payment.stripe;

import static com.carshare.rentalsystem.service.rental.RentalStatusChecker.DEFAULT_PAGE_INDEX;
import static com.carshare.rentalsystem.service.rental.RentalStatusChecker.DEFAULT_PAGE_SIZE;

import com.carshare.rentalsystem.dto.payment.even.dto.PaymentExpiredEventDto;
import com.carshare.rentalsystem.mapper.PaymentMapper;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentStatus;
import com.carshare.rentalsystem.repository.payment.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StripeSessionChecker {
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentMapper paymentMapper;

    @Async
    @Scheduled(fixedRate = 3_600_000)
    @Transactional(readOnly = true)
    public void checkExpiredSessions() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE_INDEX, DEFAULT_PAGE_SIZE);
        Page<Payment> page;

        do {
            page = paymentRepository.findExpiredPendingPayments(
                    PaymentStatus.PENDING, LocalDateTime.now(), pageable);
            List<Payment> expired = page.getContent();

            for (Payment payment : expired) {
                payment.setStatus(PaymentStatus.EXPIRED);
                applicationEventPublisher.publishEvent(new PaymentExpiredEventDto(
                        paymentMapper.toDto(payment),
                        payment.getRental().getUser().getId()
                ));

            }
            paymentRepository.saveAll(expired);

            pageable = pageable.next();
        } while (page.hasNext());
    }
}
