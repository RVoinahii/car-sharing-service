package com.carshare.rentalsystem.dto.rental;

import com.carshare.rentalsystem.dto.car.CarPreviewResponseDto;
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualReturnDate;

    private CarPreviewResponseDto car;
    private Long userId;
}
