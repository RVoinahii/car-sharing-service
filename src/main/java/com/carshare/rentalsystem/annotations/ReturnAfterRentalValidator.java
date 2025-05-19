package com.carshare.rentalsystem.annotations;

import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReturnAfterRentalValidator implements ConstraintValidator<
        ReturnAfterRentalDate, CreateRentalRequestDto> {

    @Override
    public boolean isValid(CreateRentalRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getRentalDate() == null || dto.getReturnDate() == null) {
            return true;
        }
        return dto.getReturnDate().isAfter(dto.getRentalDate());
    }
}

