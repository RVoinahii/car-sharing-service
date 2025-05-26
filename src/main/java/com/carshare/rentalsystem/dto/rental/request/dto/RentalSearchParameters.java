package com.carshare.rentalsystem.dto.rental.request.dto;

import com.carshare.rentalsystem.model.Rental;

public record RentalSearchParameters(
        String userId,
        Rental.RentalStatus status
) {
}
