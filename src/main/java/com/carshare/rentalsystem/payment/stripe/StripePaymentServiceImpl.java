package com.carshare.rentalsystem.payment.stripe;

import com.carshare.rentalsystem.dto.payment.CreatePaymentRequestDto;
import com.carshare.rentalsystem.dto.payment.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.PaymentResponseDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.mapper.PaymentMapper;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentType;
import com.carshare.rentalsystem.model.PaymentSession;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.payment.PaymentProvider;
import com.carshare.rentalsystem.repository.payment.PaymentRepository;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements StripePaymentService {
    public static final int PAYMENT_SESSION_EXPIRATION_HOURS = 24;

    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal PAYMENT_MULTIPLIER = BigDecimal.ONE;
    private static final long MIN_OVERDUE_DAYS = 1L;
    private static final String RENTAL_PAYMENT_DESCRIPTION = "Rental payment for car ";

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentMapper paymentMapper;

    @Override
    public Page<PaymentResponseDto> getAllPayments(Long userId, Pageable pageable) {
        if (userId == null) {
            return paymentRepository.findAll(pageable).map(paymentMapper::toDto);
        }

        return paymentRepository.findAllByRentalUserId(userId, pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    public PaymentPreviewResponseDto createStripeSession(CreatePaymentRequestDto requestDto,
                                                         Long userId) {
        if (!canBorrowCars(userId)) {
            throw new IllegalStateException("User already have rent!");
        }

        Rental rental = rentalRepository.findByIdAndUserId(requestDto.rentalId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find rental with id: "
                        + requestDto.rentalId()));

        LocalDate plannedReturn = rental.getReturnDate();
        LocalDate actualReturn = rental.getActualReturnDate();

        if (actualReturn == null) {
            throw new IllegalStateException("Can't create session for open rental.");
        }

        boolean isOverdue = actualReturn.isAfter(plannedReturn);
        PaymentType paymentType = requestDto.paymentType();

        validatePaymentType(paymentType, isOverdue);
        BigDecimal amountToPay = calculateAmountToPay(rental, paymentType, isOverdue);

        PaymentSession session = paymentProvider.createSession(
                RENTAL_PAYMENT_DESCRIPTION + rental.getCar().getBrand(), amountToPay);
        Payment payment = createPayment(paymentType, rental, session, amountToPay);

        return paymentMapper.toPreviewDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentPreviewResponseDto renewStripeSession(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndRentalUserId(paymentId, userId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );

        if (payment.getStatus() != Payment.PaymentStatus.EXPIRED) {
            throw new IllegalStateException("Only expired payments can be renewed.");
        }

        Rental rental = payment.getRental();
        BigDecimal amountToPay = payment.getAmountToPay();
        PaymentSession session = paymentProvider.createSession(RENTAL_PAYMENT_DESCRIPTION
                        + rental.getCar().getBrand(), amountToPay);

        payment.setSessionId(session.getSessionId());
        payment.setSessionUrl(session.getSessionUrl());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiredAt(LocalDateTime.now().plusHours(PAYMENT_SESSION_EXPIRATION_HOURS));

        return paymentMapper.toPreviewDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDto handleSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );

        payment.setStatus(Payment.PaymentStatus.PAID);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public String handleCancel() {
        return "Payment was cancelled. The session is available for 24 hours. "
                + "You can try again later.";
    }

    private boolean canBorrowCars(Long userId) {
        List<Payment> pendingPayments = paymentRepository.findByRentalUserIdAndStatus(userId,
                Payment.PaymentStatus.PENDING);

        return pendingPayments.isEmpty();
    }

    private void validatePaymentType(PaymentType type, boolean isOverdue) {
        if (type == PaymentType.FINE && !isOverdue) {
            throw new IllegalStateException("Cannot create FINE payment if rental "
                    + "is not overdue. Use PAYMENT instead.");
        }

        if (type == PaymentType.PAYMENT && isOverdue) {
            throw new IllegalStateException("Cannot create PAYMENT if rental is "
                    + "overdue. Use FINE instead.");
        }
    }

    private BigDecimal calculateAmountToPay(Rental rental, PaymentType type, boolean isOverdue) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();

        return switch (type) {
            case FINE -> {
                long overdueDays = Math.max(
                        ChronoUnit.DAYS.between(rental.getReturnDate(),
                                rental.getActualReturnDate()), MIN_OVERDUE_DAYS
                );
                yield dailyFee.multiply(FINE_MULTIPLIER).multiply(BigDecimal.valueOf(overdueDays));
            }
            case PAYMENT -> dailyFee.multiply(PAYMENT_MULTIPLIER);
        };
    }

    private Payment createPayment(PaymentType paymentType, Rental rental,
                                  PaymentSession session, BigDecimal amountToPay) {
        Payment payment = new Payment();
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setType(paymentType);
        payment.setRental(rental);
        payment.setSessionUrl(session.getSessionUrl());
        payment.setSessionId(session.getSessionId());
        payment.setAmountToPay(amountToPay);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiredAt(LocalDateTime.now().plusHours(PAYMENT_SESSION_EXPIRATION_HOURS));

        return payment;
    }
}
