package com.carshare.rentalsystem.service.payment;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Rental;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentalPaymentCalculator {
    public static final int DAYS_INCLUSIVE_OFFSET = 1;

    private static final long THREE_PERCENTS_DISCOUNT_THRESHOLD = 4;
    private static final long FIVE_PERCENTS_DISCOUNT_THRESHOLD = 8;
    private static final long MIN_DAYS_FOR_DISCOUNT = 4;
    private static final BigDecimal CANCELLATION_FEE_RATE = new BigDecimal("0.15");
    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal EARLY_RETURN_FEE_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_8_TO_14_DAYS = new BigDecimal("0.05");
    private static final BigDecimal DISCOUNT_4_TO_8_DAYS = new BigDecimal("0.03");

    public PaymentCalculationResult calculatePayment(Rental rental) {
        LocalDate rentalStartDate = rental.getRentalDate();
        LocalDate plannedReturnDate = rental.getReturnDate();
        LocalDate actualReturnDate = rental.getActualReturnDate();

        BigDecimal dailyFee = rental.getCar().getDailyFee();

        long plannedDays = ChronoUnit.DAYS.between(rentalStartDate, plannedReturnDate)
                + DAYS_INCLUSIVE_OFFSET;
        long actualDays = ChronoUnit.DAYS.between(rentalStartDate, actualReturnDate)
                + DAYS_INCLUSIVE_OFFSET;

        BigDecimal baseAmount = dailyFee.multiply(BigDecimal.valueOf(actualDays));
        BigDecimal discount = calculateDiscount(actualDays, baseAmount);

        PaymentCalculationType type = PaymentCalculationType.from(rental);

        return switch (type) {
            case CANCELLATION -> calculateCancellationFee(plannedDays, baseAmount);
            case EARLY_RETURN -> calculateEarlyReturnPayment(plannedDays, actualDays,
                    baseAmount, discount, dailyFee);
            case ON_TIME_RETURN -> calculateStandardPayment(baseAmount, discount);
            case LATE_RETURN -> calculateFine(plannedReturnDate, actualReturnDate, dailyFee);
        };
    }

    private BigDecimal calculateDiscount(long actualDays, BigDecimal baseAmount) {
        if (actualDays >= FIVE_PERCENTS_DISCOUNT_THRESHOLD) {
            return baseAmount.multiply(DISCOUNT_8_TO_14_DAYS);
        } else if (actualDays >= THREE_PERCENTS_DISCOUNT_THRESHOLD) {
            return baseAmount.multiply(DISCOUNT_4_TO_8_DAYS);
        }
        return BigDecimal.ZERO;
    }

    private PaymentCalculationResult calculateCancellationFee(long plannedDays,
            BigDecimal baseAmount) {
        BigDecimal fee = BigDecimal.ZERO;
        if (plannedDays >= MIN_DAYS_FOR_DISCOUNT) {
            fee = baseAmount.multiply(CANCELLATION_FEE_RATE);
        }
        return new PaymentCalculationResult(Payment.PaymentType.CANCELLATION_FEE, fee);
    }

    private PaymentCalculationResult calculateFine(LocalDate plannedReturnDate,
            LocalDate actualReturnDate, BigDecimal dailyFee) {
        long overdueDays = ChronoUnit.DAYS.between(plannedReturnDate, actualReturnDate);
        BigDecimal fineAmount = dailyFee.multiply(BigDecimal.valueOf(overdueDays))
                .multiply(FINE_MULTIPLIER);
        return new PaymentCalculationResult(Payment.PaymentType.FINE, fineAmount);
    }

    private PaymentCalculationResult calculateEarlyReturnPayment(long plannedDays, long actualDays,
            BigDecimal baseAmount, BigDecimal discount, BigDecimal dailyFee) {
        long unusedDays = plannedDays - actualDays;
        BigDecimal unusedAmount = dailyFee.multiply(BigDecimal.valueOf(unusedDays));
        BigDecimal earlyReturnFee = unusedAmount.multiply(EARLY_RETURN_FEE_RATE);
        BigDecimal total = baseAmount.subtract(discount).add(earlyReturnFee);
        return new PaymentCalculationResult(Payment.PaymentType.PAYMENT, total);
    }

    private PaymentCalculationResult calculateStandardPayment(BigDecimal baseAmount,
                                                              BigDecimal discount) {
        BigDecimal total = baseAmount.subtract(discount);
        return new PaymentCalculationResult(Payment.PaymentType.PAYMENT, total);
    }

    public record PaymentCalculationResult(
            Payment.PaymentType paymentType,
            BigDecimal amount) {
    }
}
