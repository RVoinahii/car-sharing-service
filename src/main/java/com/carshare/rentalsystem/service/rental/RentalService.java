package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.RentalDetailsResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalSearchParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto);

    Page<RentalDetailsResponseDto> getRentalsById(Long userId,
                                                  Pageable pageable);

    Page<RentalDetailsResponseDto> getSpecificRentals(RentalSearchParameters params,
                                                      Pageable pageable);

    RentalDetailsResponseDto getRentalInfo(Long userId, Long rentalId);

    RentalDetailsResponseDto returnRental(Long userId, Long rentalId);
}
