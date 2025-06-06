package com.carshare.rentalsystem.dto.car.request.dto;

import com.carshare.rentalsystem.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank
        String model,

        @NotBlank
        String brand,

        @NotNull
        Car.Type type,

        @Positive
        int inventory,

        @NotNull
        @Positive
        BigDecimal dailyFee
) {
}
