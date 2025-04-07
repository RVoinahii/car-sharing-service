package com.carshare.rentalsystem.service;

import com.carshare.rentalsystem.dto.CarDto;
import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.mapper.CarMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.CarRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto create(CreateCarRequestDto carDto) {
        Car car = carMapper.toEntity(carDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public List<CarDto> getAll() {
        return carRepository.findAll().stream()
                .map(carMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CarDto getById(Long carId) {
        Car carById = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + carId)
        );
        return carMapper.toDto(carById);
    }

    @Override
    public CarDto updateById(Long carId, CreateCarRequestDto updatedCarDataDto) {
        Car existingCar = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + carId)
        );
        carMapper.updateCarFromDto(updatedCarDataDto, existingCar);
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Override
    public void deleteById(Long carId) {
        carRepository.deleteById(carId);
    }
}
