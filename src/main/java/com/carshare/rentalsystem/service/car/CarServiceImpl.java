package com.carshare.rentalsystem.service.car;

import com.carshare.rentalsystem.dto.car.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.CarResponseDto;
import com.carshare.rentalsystem.dto.car.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.InventoryUpdateRequestDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.mapper.CarMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.car.CarRepository;
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
    public Page<CarPreviewResponseDto> getAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toCarPreviewDto);
    }

    @Override
    public CarResponseDto getById(Long carId) {
        Car carById = getCarById(carId);
        return carMapper.toDto(carById);
    }

    @Override
    public CarResponseDto create(CreateCarRequestDto carDto) {
        Car car = carMapper.toEntity(carDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public CarResponseDto updateCarById(Long carId, CreateCarRequestDto updatedCarDataDto) {
        Car existingCar = getCarById(carId);
        carMapper.updateCarFromDto(updatedCarDataDto, existingCar);
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Override
    public CarResponseDto updateInventoryByCarId(Long carId,
            InventoryUpdateRequestDto inventoryDto) {
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
