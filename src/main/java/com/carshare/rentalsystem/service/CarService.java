package com.carshare.rentalsystem.service;

import com.carshare.rentalsystem.dto.CarDto;
import com.carshare.rentalsystem.dto.CarPreviewDto;
import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.InventoryUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    Page<CarPreviewDto> getAll(Pageable pageable);

    CarDto getById(Long carId);

    CarDto create(CreateCarRequestDto carDto);

    CarDto updateCarById(Long carId, CreateCarRequestDto carDto);

    CarDto updateInventoryByCarId(Long carId, InventoryUpdateDto inventoryDto);

    void deleteById(Long carId);
}
