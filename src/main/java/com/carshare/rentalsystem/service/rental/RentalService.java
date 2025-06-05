package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto);

    Page<RentalPreviewResponseDto> getRentalsById(Long userId,
                                                  Pageable pageable);

    Page<RentalPreviewResponseDto> getSpecificRentals(RentalSearchParameters params,
                                               Pageable pageable);

    RentalResponseDto getAnyRentalInfo(Long rentalId);

    RentalResponseDto getCustomerRentalInfo(Long userId, Long rentalId);

    RentalResponseDto returnRental(Long userId, Long rentalId);
}
