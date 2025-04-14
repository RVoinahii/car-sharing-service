package com.carshare.rentalsystem.dto.rental;

public record RentalSearchParameters(
        String userId,
        Boolean isActive
) {
}
