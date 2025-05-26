package com.carshare.rentalsystem.service.payment;

import com.carshare.rentalsystem.model.Rental;
import java.time.LocalDate;

public enum PaymentCalculationType {
    CANCELLATION,
    EARLY_RETURN,
    ON_TIME_RETURN,
    LATE_RETURN;

    public static PaymentCalculationType from(Rental rental) {
        if (rental.getActualReturnDate() == null) {
            return CANCELLATION;
        }

        LocalDate planned = rental.getReturnDate();
        LocalDate actual = rental.getActualReturnDate();

        if (actual.isBefore(planned)) {
            return EARLY_RETURN;
        }

        if (actual.isAfter(planned)) {
            return LATE_RETURN;
        }

        return ON_TIME_RETURN;
    }
}
