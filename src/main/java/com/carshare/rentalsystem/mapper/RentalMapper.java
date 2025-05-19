package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CarMapper.class, UserMapper.class})
public interface RentalMapper {
    @Named("toDto")
    @Mapping(target = "car", source = "car", qualifiedByName = "carToPreviewDto")
    @Mapping(target = "user", source = "user", qualifiedByName = "userToPreviewDto")
    RentalResponseDto toDto(Rental rental);

    @Mapping(target = "car", source = "carId", qualifiedByName = "carById")
    Rental toEntity(CreateRentalRequestDto requestDto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "active", source = "rental", qualifiedByName = "isRentalActive")
    RentalPreviewResponseDto toPreviewDto(Rental rental);

    @Named("isRentalActive")
    default boolean isRentalActive(Rental rental) {
        return rental.getActualReturnDate() == null;
    }
}
