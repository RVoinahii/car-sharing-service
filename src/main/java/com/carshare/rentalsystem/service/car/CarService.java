package com.carshare.rentalsystem.service.car;

import com.carshare.rentalsystem.dto.car.request.dto.CarSearchParameters;
import com.carshare.rentalsystem.dto.car.request.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.request.dto.InventoryUpdateRequestDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    Page<CarPreviewResponseDto> getAll(CarSearchParameters searchParameters,
                                       Pageable pageable);

    CarResponseDto getById(Long carId);

    CarResponseDto create(CreateCarRequestDto carDto);

    CarResponseDto updateCarById(Long carId, CreateCarRequestDto carDto);

    CarResponseDto updateInventoryByCarId(Long carId, InventoryUpdateRequestDto inventoryDto);

    void deleteById(Long carId);
}
