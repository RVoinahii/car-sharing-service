package com.carshare.rentalsystem.payment.stripe;

import com.carshare.rentalsystem.dto.payment.CreatePaymentRequestDto;
import com.carshare.rentalsystem.dto.payment.PaymentCancelResponseDto;
import com.carshare.rentalsystem.dto.payment.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.PaymentResponseDto;
import com.carshare.rentalsystem.exception.ActiveRentalAlreadyExistsException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.InvalidPaymentTypeException;
import com.carshare.rentalsystem.exception.PaymentNotExpiredException;
import com.carshare.rentalsystem.exception.RentalNotFinishedException;
import com.carshare.rentalsystem.mapper.PaymentMapper;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentType;
import com.carshare.rentalsystem.model.PaymentSession;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.notifications.NotificationGeneratorService;
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
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements StripePaymentService {
    public static final int PAYMENT_SESSION_EXPIRATION_HOURS = 24;

    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal PAYMENT_MULTIPLIER = BigDecimal.ONE;
    private static final long MIN_OVERDUE_DAYS = 1L;
    private static final String RENTAL_PAYMENT_DESCRIPTION = "Rental payment for car ";
    private static final String CANCEL_PAYMENT_MESSAGE = "Payment was cancelled."
            + " The session is available for 24 hours. You can try again later.";

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentMapper paymentMapper;
    private final NotificationGeneratorService notificationGeneratorService;

    @Transactional(readOnly = true)
    @Override
    public Page<PaymentResponseDto> getAllPayments(Long userId, Pageable pageable) {
        if (userId == null) {
            return paymentRepository.findAllWithRentalAndCar(pageable).map(paymentMapper::toDto);
        }

        return paymentRepository.findAllByRentalUserIdWithRentalAndUser(userId, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional
    @Override
    public PaymentPreviewResponseDto createStripeSession(CreatePaymentRequestDto requestDto,
                                                         Long userId) {
        if (!canBorrowCars(userId)) {
            throw new ActiveRentalAlreadyExistsException("User already has an active rent!");
        }

        Rental rental = rentalRepository.findByIdAndUserIdWithCarAndUser(
                requestDto.rentalId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find rental with id: "
                        + requestDto.rentalId()));

        LocalDate plannedReturn = rental.getReturnDate();
        LocalDate actualReturn = rental.getActualReturnDate();

        if (actualReturn == null) {
            throw new RentalNotFinishedException("Can't create session for an open rental.");
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

    @Transactional
    @Override
    public PaymentPreviewResponseDto renewStripeSession(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndRentalUserId(paymentId, userId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );

        if (payment.getStatus() != Payment.PaymentStatus.EXPIRED) {
            throw new PaymentNotExpiredException("Only expired payments can be renewed.");
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

        notificationGeneratorService.notifyManagersAboutRenewPayment(payment);
        notificationGeneratorService.notifyCustomerAboutRenewPayment(payment, userId);

        return paymentMapper.toPreviewDto(paymentRepository.save(payment));
    }

    @Transactional
    @Override
    public PaymentResponseDto handleSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionIdWithRentalAndUser(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );

        payment.setStatus(Payment.PaymentStatus.PAID);
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDto responseDto = paymentMapper.toDto(savedPayment);

        notificationGeneratorService.notifyManagersAboutSuccessfulPayment(payment);
        notificationGeneratorService.notifyCustomerAboutSuccessfulPayment(
                payment, payment.getRental().getUser().getId());

        return responseDto;
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentCancelResponseDto handleCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionIdWithRentalAndUser(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );
        PaymentCancelResponseDto responseDto = paymentMapper.toCancelDto(payment);
        responseDto.setCancelMessage(CANCEL_PAYMENT_MESSAGE);

        notificationGeneratorService.notifyManagersAboutPaymentCancel(payment);
        notificationGeneratorService.notifyCustomerAboutPaymentCancel(
                payment, payment.getRental().getUser().getId());

        return responseDto;
    }

    private boolean canBorrowCars(Long userId) {
        List<Payment> pendingPayments = paymentRepository.findByRentalUserIdAndStatus(userId,
                Payment.PaymentStatus.PENDING);

        return pendingPayments.isEmpty();
    }

    private void validatePaymentType(PaymentType type, boolean isOverdue) {
        if (type == PaymentType.FINE && !isOverdue) {
            throw new InvalidPaymentTypeException("Cannot create FINE payment if rental "
                    + "is not overdue. Use PAYMENT instead.");
        }

        if (type == PaymentType.PAYMENT && isOverdue) {
            throw new InvalidPaymentTypeException("Cannot create PAYMENT if rental is "
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
