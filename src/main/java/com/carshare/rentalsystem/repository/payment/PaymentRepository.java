package com.carshare.rentalsystem.repository.payment;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long>,
        PagingAndSortingRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.sessionId = :sessionId")
    Optional<Payment> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT p FROM Payment p WHERE p.rental.user.id = :userId")
    Page<Payment> findAllByRentalUserId(@Param("userId") Long userId,
                                                         Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId AND p.rental.user.id = :userId")
    Optional<Payment> findByIdAndRentalUserId(
            @Param("paymentId") Long paymentId, @Param("userId") Long userId);

    List<Payment> findByRentalUserIdAndStatus(Long userId, PaymentStatus status);
}
