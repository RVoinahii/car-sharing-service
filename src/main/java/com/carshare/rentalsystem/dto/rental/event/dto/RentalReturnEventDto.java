package com.carshare.rentalsystem.dto.rental.event.dto;

import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;

public record RentalReturnEventDto(RentalResponseDto rental, Long userId) {
}
