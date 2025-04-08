package com.carshare.rentalsystem.dto;

import jakarta.validation.constraints.Min;

public record InventoryUpdateDto(
        @Min(0)
        int inventory
) {
}
