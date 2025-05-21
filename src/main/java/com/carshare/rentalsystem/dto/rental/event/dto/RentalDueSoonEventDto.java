package com.carshare.rentalsystem.dto.rental.event.dto;

import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;

public record RentalDueSoonEventDto(RentalResponseDto rental, Long userId) {
}
