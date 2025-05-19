package com.carshare.rentalsystem.annotations;

import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

public class AtLeastOneFieldNotEmptyValidator
        implements ConstraintValidator<AtLeastOneFieldNotEmpty, UserUpdateRequestDto> {
    @Override
    public boolean isValid(UserUpdateRequestDto updateRequestDto,
                           ConstraintValidatorContext context) {
        return Stream.of(updateRequestDto.email(),
                         updateRequestDto.firstName(),
                         updateRequestDto.lastName())
                .anyMatch(value -> value != null && !value.trim().isEmpty());
    }
}
