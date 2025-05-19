package com.carshare.rentalsystem.dto.rental.response.dto;

public record RentalSearchParameters(
        String userId,
        Boolean isActive
) {
}
