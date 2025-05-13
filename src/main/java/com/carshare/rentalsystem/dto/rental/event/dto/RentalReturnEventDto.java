package com.carshare.rentalsystem.dto.rental.event.dto;

import com.carshare.rentalsystem.model.Rental;

public record RentalReturnEventDto(Rental rental, Long userId) {
}
