package com.carshare.rentalsystem.dto.rental.event.dto;

import com.carshare.rentalsystem.model.Rental;

public record RentalCreatedEventDto(Rental rental, Long userId) {
}
