package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalSearchParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto);

    Page<RentalResponseDto> getRentalsById(Long userId,
                                           Pageable pageable);

    Page<RentalResponseDto> getSpecificRentals(RentalSearchParameters params,
                                               Pageable pageable);

    RentalResponseDto getRentalInfo(Long userId, Long rentalId);

    RentalResponseDto returnRental(Long userId, Long rentalId);
}
