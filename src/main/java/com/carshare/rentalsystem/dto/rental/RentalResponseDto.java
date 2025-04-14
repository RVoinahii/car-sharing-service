package com.carshare.rentalsystem.dto.rental;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RentalResponseDto {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rentalDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate;

    private Long carId;
    private Long userId;
}
