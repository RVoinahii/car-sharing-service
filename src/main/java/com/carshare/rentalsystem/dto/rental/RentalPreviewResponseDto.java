package com.carshare.rentalsystem.dto.rental;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RentalPreviewResponseDto {
    private Long id;
    private boolean active;
    private Long userId;
}
