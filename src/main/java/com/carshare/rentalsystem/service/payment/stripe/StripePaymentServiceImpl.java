package com.carshare.rentalsystem.service.payment.stripe;

import com.carshare.rentalsystem.dto.payment.even.dto.PaymentCancelEventDto;
import com.carshare.rentalsystem.dto.payment.even.dto.PaymentSuccessEventDto;
import com.carshare.rentalsystem.dto.payment.even.dto.RenewPaymentEvenDto;
import com.carshare.rentalsystem.dto.payment.request.dto.CreatePaymentRequestDto;
import com.carshare.rentalsystem.dto.payment.request.dto.PaymentSearchParameters;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentCancelResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.exception.ActiveSessionAlreadyExistsException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.PaymentNotExpiredException;
import com.carshare.rentalsystem.exception.RentalNotFinishedException;
import com.carshare.rentalsystem.mapper.PaymentMapper;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Payment.PaymentType;
import com.carshare.rentalsystem.model.PaymentSession;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.payment.PaymentRepository;
import com.carshare.rentalsystem.repository.payment.PaymentSpecificationBuilder;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import com.carshare.rentalsystem.service.payment.PaymentProvider;
import com.carshare.rentalsystem.service.payment.RentalPaymentCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements StripePaymentService {
    public static final int PAYMENT_SESSION_EXPIRATION_HOURS = 24;

    private static final String RENTAL_PAYMENT_DESCRIPTION = "Rental payment for car ";
    private static final String CANCEL_PAYMENT_MESSAGE = "Payment was cancelled."
            + " The session is available for 24 hours. You can try again later.";

    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentMapper paymentMapper;
    private final PaymentSpecificationBuilder paymentSpecificationBuilder;
    private final RentalPaymentCalculator rentalPaymentCalculator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    @Override
    public Page<PaymentResponseDto> getPaymentsById(Long userId, Pageable pageable) {
        return paymentRepository.findAllByRentalUserId(userId, pageable).map(paymentMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PaymentResponseDto> getSpecificPayments(PaymentSearchParameters searchParameters,
                                                        Pageable pageable) {
        Specification<Payment> paymentSpecification =
                paymentSpecificationBuilder.build(searchParameters);

        return paymentRepository.findAll(paymentSpecification, pageable)
                .map(paymentMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentResponseDto getAnyPaymentInfo(Long paymentId) {
        Payment payment = findPaymentById(paymentId);

        return paymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentResponseDto getCustomerPaymentInfo(Long userId, Long paymentId) {
        Payment payment = findPaymentById(paymentId);

        if (!payment.getRental().getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Can't find paymentId with id: " + paymentId);
        }

        return paymentMapper.toDto(payment);
    }

    @Transactional
    @Override
    public PaymentPreviewResponseDto createStripeSession(CreatePaymentRequestDto requestDto,
                                                         Long userId) {
        if (hasPendingPaymentSession(userId)) {
            throw new ActiveSessionAlreadyExistsException("User already has an active session!");
        }

        Rental rental = rentalRepository.findByIdAndUserIdWithCarAndUser(
                requestDto.rentalId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find rental with id: "
                        + requestDto.rentalId()));

        if (rental.getStatus() != Rental.RentalStatus.WAITING_FOR_PAYMENT) {
            throw new RentalNotFinishedException("Can't create payment for this rental.");
        }

        RentalPaymentCalculator.PaymentCalculationResult calculationResult =
                rentalPaymentCalculator.calculatePayment(rental);

        PaymentSession session = paymentProvider.createSession(
                RENTAL_PAYMENT_DESCRIPTION + rental.getCar().getBrand(),
                calculationResult.amount()
        );

        Payment payment = createPayment(
                calculationResult.paymentType(),
                rental,
                session,
                calculationResult.amount()
        );

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

        PaymentSession session = paymentProvider.createSession(RENTAL_PAYMENT_DESCRIPTION
                        + payment.getRental().getCar().getBrand(), payment.getAmountToPay());

        payment.setSessionId(session.getSessionId());
        payment.setSessionUrl(session.getSessionUrl());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiredAt(LocalDateTime.now().plusHours(PAYMENT_SESSION_EXPIRATION_HOURS));

        PaymentResponseDto responseDto = paymentMapper.toDto(payment);

        applicationEventPublisher.publishEvent(new RenewPaymentEvenDto(responseDto, userId));

        return paymentMapper.toPreviewDto(paymentRepository.save(payment));
    }

    @Transactional
    @Override
    public PaymentResponseDto handleSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionIdWithRental(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );

        resolveRentalStatus(payment.getRental());

        payment.setStatus(Payment.PaymentStatus.PAID);
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDto responseDto = paymentMapper.toDto(savedPayment);

        applicationEventPublisher.publishEvent(new PaymentSuccessEventDto(responseDto,
                payment.getRental().getUser().getId()));

        return responseDto;
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentCancelResponseDto handleCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found.")
        );
        PaymentCancelResponseDto responseDto = paymentMapper.toCancelDto(payment);
        PaymentResponseDto responseDto1 = paymentMapper.toDto(payment);
        responseDto.setCancelMessage(CANCEL_PAYMENT_MESSAGE);

        applicationEventPublisher.publishEvent(new PaymentCancelEventDto(responseDto1,
                payment.getRental().getUser().getId()));

        return responseDto;
    }

    private boolean hasPendingPaymentSession(Long userId) {
        return paymentRepository.existsPendingPaymentForUser(userId,
                Payment.PaymentStatus.PENDING);
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

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with ID: " + paymentId)
        );
    }

    private void resolveRentalStatus(Rental rental) {
        rental.setStatus(
                rental.getActualReturnDate() == null
                        ? Rental.RentalStatus.CANCELLED
                        : Rental.RentalStatus.COMPLETED
        );
        rentalRepository.save(rental);
    }
}
