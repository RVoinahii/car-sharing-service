package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.CarDto;
import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toEntity(CreateCarRequestDto requestDto);

    void updateCarFromDto(CreateCarRequestDto updatedCar, @MappingTarget Car existingCar);
}
