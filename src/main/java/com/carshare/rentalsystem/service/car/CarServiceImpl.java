package com.carshare.rentalsystem.service.car;

import com.carshare.rentalsystem.dto.car.request.dto.CarSearchParameters;
import com.carshare.rentalsystem.dto.car.request.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.request.dto.InventoryUpdateRequestDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarResponseDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.mapper.CarMapper;
import com.carshare.rentalsystem.mapper.search.CarSearchParametersMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationBuilderFactory;
import com.carshare.rentalsystem.repository.car.CarRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarSearchParametersMapper searchParametersMapper;
    private final SpecificationBuilderFactory specificationBuilderFactory;

    @Transactional(readOnly = true)
    @Override
    public Page<CarPreviewResponseDto> getAll(CarSearchParameters searchParameters,
                                              Pageable pageable) {
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<Car> carSpecification = specificationBuilderFactory
                .getBuilder(Car.class)
                .build(filters);

        return carRepository.findAll(carSpecification, pageable)
                .map(carMapper::toPreviewDto);
    }

    @Transactional(readOnly = true)
    @Override
    public CarResponseDto getById(Long carId) {
        Car carById = getCarById(carId);
        return carMapper.toDto(carById);
    }

    @Transactional
    @Override
    public CarResponseDto create(CreateCarRequestDto carDto) {
        Car car = carMapper.toEntity(carDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Transactional
    @Override
    public CarResponseDto updateCarById(Long carId, CreateCarRequestDto updatedCarDataDto) {
        Car existingCar = getCarById(carId);
        carMapper.updateCarFromDto(updatedCarDataDto, existingCar);
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Transactional
    @Override
    public CarResponseDto updateInventoryByCarId(Long carId,
            InventoryUpdateRequestDto inventoryDto) {
        Car existingCar = getCarById(carId);
        existingCar.setInventory(inventoryDto.inventory());
        return carMapper.toDto(carRepository.save(existingCar));
    }

    @Transactional
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
