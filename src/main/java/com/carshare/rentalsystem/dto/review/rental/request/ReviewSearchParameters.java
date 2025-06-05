package com.carshare.rentalsystem.dto.review.rental.request;

import com.carshare.rentalsystem.model.Car;

public record ReviewSearchParameters(
        String carId,
        String model,
        String brand,
        Car.Type type
) {
}
