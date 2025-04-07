package com.carshare.rentalsystem.service;

import com.carshare.rentalsystem.dto.CarDto;
import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import java.util.List;

public interface CarService {
    CarDto create(CreateCarRequestDto carDto);

    List<CarDto> getAll();

    CarDto getById(Long carId);

    CarDto updateById(Long carId, CreateCarRequestDto carDto);

    void deleteById(Long carId);
}
