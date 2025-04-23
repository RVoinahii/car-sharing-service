package com.carshare.rentalsystem.repository.payment;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>,
        PagingAndSortingRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    Page<Payment> findAllByRentalUserId(Long userId, Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByIdAndRentalUserId(Long paymentId, Long userId);

    List<Payment> findByRentalUserIdAndStatus(Long userId, PaymentStatus status);
}
