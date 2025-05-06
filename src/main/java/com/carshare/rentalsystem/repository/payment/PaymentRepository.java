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

    @Query("SELECT p FROM Payment p JOIN FETCH p.rental r JOIN FETCH r.user u "
            + "WHERE p.sessionId = :sessionId")
    Optional<Payment> findBySessionIdWithRentalAndUser(@Param("sessionId") String sessionId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.rental r JOIN FETCH r.user u "
            + "WHERE u.id = :userId")
    Page<Payment> findAllByRentalUserIdWithRentalAndUser(@Param("userId") Long userId,
                                                         Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p JOIN FETCH p.rental r JOIN FETCH r.car c "
            + "WHERE p.id = :paymentId AND r.user.id = :userId")
    Optional<Payment> findByIdAndRentalUserId(
            @Param("paymentId") Long paymentId, @Param("userId") Long userId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.rental r JOIN FETCH r.car c")
    Page<Payment> findAllWithRentalAndCar(Pageable pageable);

    List<Payment> findByRentalUserIdAndStatus(Long userId, PaymentStatus status);
}
