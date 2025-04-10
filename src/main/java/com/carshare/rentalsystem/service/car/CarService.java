package com.carshare.rentalsystem.service.car;

import com.carshare.rentalsystem.dto.car.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.CarResponseDto;
import com.carshare.rentalsystem.dto.car.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.InventoryUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    Page<CarPreviewResponseDto> getAll(Pageable pageable);

    CarResponseDto getById(Long carId);

    CarResponseDto create(CreateCarRequestDto carDto);

    CarResponseDto updateCarById(Long carId, CreateCarRequestDto carDto);

    CarResponseDto updateInventoryByCarId(Long carId, InventoryUpdateRequestDto inventoryDto);

    void deleteById(Long carId);
}
