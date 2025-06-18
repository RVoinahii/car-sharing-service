package com.carshare.rentalsystem.dto.car.request.dto;

import com.carshare.rentalsystem.model.Car;

public record CarSearchParameters(
        String model,
        String brand,
        Car.Type type,
        String priceRange,
        Boolean onlyAvailable
) {
}
