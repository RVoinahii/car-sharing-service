package com.carshare.rentalsystem.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReturnAfterRentalValidator.class)
public @interface ReturnAfterRentalDate {
    String message() default "Return date must be after rental date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
