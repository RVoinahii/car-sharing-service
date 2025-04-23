package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.rental.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = CarMapper.class)
public interface RentalMapper {
    @Named("toDto")
    @Mapping(target = "car", source = "car", qualifiedByName = "toPreviewDto")
    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental rental);

    @Mapping(target = "car", source = "carId", qualifiedByName = "carById")
    Rental toEntity(CreateRentalRequestDto requestDto);
}
