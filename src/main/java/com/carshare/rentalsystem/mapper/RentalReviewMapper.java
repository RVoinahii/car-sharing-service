package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.review.rental.response.RentalReviewResponseDto;
import com.carshare.rentalsystem.dto.review.rental.response.ReviewPreviewResponseDto;
import com.carshare.rentalsystem.model.RentalReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = RentalMapper.class)
public interface RentalReviewMapper {

    @Mapping(target = "rentalPreview", source = "rental", qualifiedByName = "rentalToPreviewDto")
    @Mapping(target = "overallImpression", source = "overallImpression",
            qualifiedByName = "mapOverallImpression")
    @Mapping(target = "media", ignore = true)
    RentalReviewResponseDto toDto(RentalReview review);

    @Mapping(target = "carId", source = "rental.car.id")
    @Mapping(target = "overallImpression", source = "overallImpression",
            qualifiedByName = "mapOverallImpression")
    ReviewPreviewResponseDto toPreviewDto(RentalReview review);

    @Named("mapOverallImpression")
    default String mapOverallImpression(RentalReview.OverallImpression impression) {
        return impression.name();
    }
}
