package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.car.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.CarResponseDto;
import com.carshare.rentalsystem.dto.car.CreateCarRequestDto;
import com.carshare.rentalsystem.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarResponseDto toDto(Car car);

    Car toEntity(CreateCarRequestDto requestDto);

    CarPreviewResponseDto toCarPreviewDto(Car car);

    void updateCarFromDto(CreateCarRequestDto updatedCar, @MappingTarget Car existingCar);
}
