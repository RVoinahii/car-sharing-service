package com.carshare.rentalsystem.dto.car;

import jakarta.validation.constraints.Min;

public record InventoryUpdateRequestDto(
        @Min(0)
        int inventory
) {
}
