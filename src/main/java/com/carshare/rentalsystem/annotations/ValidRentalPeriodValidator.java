package com.carshare.rentalsystem.annotations;

import static com.carshare.rentalsystem.service.payment.RentalPaymentCalculator.DAYS_INCLUSIVE_OFFSET;

import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ValidRentalPeriodValidator implements ConstraintValidator<
        ValidRentalPeriod, CreateRentalRequestDto> {

    private static final long MAX_RENTAL_DAYS = 14;

    @Override
    public boolean isValid(CreateRentalRequestDto dto, ConstraintValidatorContext context) {
        LocalDate rentalDate = dto.getRentalDate();
        LocalDate returnDate = dto.getReturnDate();

        long days = ChronoUnit.DAYS.between(rentalDate, returnDate) + DAYS_INCLUSIVE_OFFSET;
        return returnDate.isAfter(rentalDate) && days <= MAX_RENTAL_DAYS;
    }
}

