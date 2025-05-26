package com.carshare.rentalsystem.repository.payment;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long>,
        PagingAndSortingRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.rental WHERE p.sessionId = :sessionId")
    Optional<Payment> findBySessionIdWithRental(@Param("sessionId") String sessionId);

    @Query("SELECT p FROM Payment p WHERE p.sessionId = :sessionId")
    Optional<Payment> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT p FROM Payment p WHERE p.rental.user.id = :userId")
    Page<Payment> findAllByRentalUserId(@Param("userId") Long userId,
                                                         Pageable pageable);

    @Query("""
    SELECT p
    FROM Payment p
    JOIN FETCH p.rental r
    JOIN FETCH r.user u
    WHERE p.status = :status
    AND p.expiredAt < :now
            """)
    Page<Payment> findExpiredPendingPayments(@Param("status") PaymentStatus status,
                                             @Param("now") LocalDateTime now,
                                             Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId AND p.rental.user.id = :userId")
    Optional<Payment> findByIdAndRentalUserId(
            @Param("paymentId") Long paymentId, @Param("userId") Long userId);

    @Query("""
    SELECT COUNT(p) > 0
    FROM Payment p
    WHERE p.rental.user.id = :userId
    AND p.status = :status
            """)
    boolean existsPendingPaymentForUser(@Param("userId") Long userId,
                                        @Param("status") PaymentStatus status);
}
