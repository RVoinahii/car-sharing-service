package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalSearchParameters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto);

    Page<RentalResponseDto> getRentalsById(Long userId,
                                           Pageable pageable);

    Page<RentalResponseDto> getSpecificRentals(RentalSearchParameters params,
                                               Pageable pageable);

    Page<RentalResponseDto> getAllRentalsPreview(boolean isManager, Long userId,
                                                        Pageable pageable);

    RentalResponseDto getAnyRentalInfo(Long rentalId);

    RentalResponseDto getCustomerRentalInfo(Long userId, Long rentalId);

    RentalResponseDto returnRental(Long userId, Long rentalId);
}
