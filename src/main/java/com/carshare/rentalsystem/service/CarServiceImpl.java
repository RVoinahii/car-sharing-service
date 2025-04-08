package com.carshare.rentalsystem.service;

import com.carshare.rentalsystem.dto.CarDto;
import com.carshare.rentalsystem.dto.CarPreviewDto;
import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.InventoryUpdateDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.mapper.CarMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public Page<CarPreviewDto> getAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toCarPreviewDto);
    }

    @Override
    public CarDto getById(Long carId) {
        Car carById = getCarById(carId);
        return carMapper.toDto(carById);
    }

    @Override
    public CarDto create(CreateCarRequestDto carDto) {
        Car car = carMapper.toEntity(carDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public CarDto updateCarById(Long carId, CreateCarRequestDto updatedCarDataDto) {
        Car existingCar = getCarById(carId);
        carMapper.updateCarFromDto(updatedCarDataDto, existingCar);
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Override
    public CarDto updateInventoryByCarId(Long carId, InventoryUpdateDto inventoryDto) {
        Car existingCar = getCarById(carId);
        existingCar.setInventory(inventoryDto.inventory());
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Override
    public void deleteById(Long carId) {
        carRepository.deleteById(carId);
    }

    private Car getCarById(Long carId) {
        return carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + carId)
        );
    }
}
