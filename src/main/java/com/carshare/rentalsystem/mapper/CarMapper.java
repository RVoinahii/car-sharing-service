package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.car.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.CarResponseDto;
import com.carshare.rentalsystem.dto.car.CreateCarRequestDto;
import com.carshare.rentalsystem.model.Car;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper {
    CarResponseDto toDto(Car car);

    Car toEntity(CreateCarRequestDto requestDto);

    CarPreviewResponseDto toCarPreviewDto(Car car);

    void updateCarFromDto(CreateCarRequestDto updatedCar, @MappingTarget Car existingCar);

    @Named("carById")
    default Car carById(Long id) {
        return Optional.ofNullable(id)
                .map(Car::new)
                .orElse(null);
    }
}
