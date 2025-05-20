package com.carshare.rentalsystem.dto.rental.response.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RentalPreviewResponseDto {
    private Long id;
    private Long userId;
    private Long carId;
}
