package com.carshare.rentalsystem.repository.review.rental;

import com.carshare.rentalsystem.dto.review.rental.request.ReviewSearchParameters;
import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationBuilder;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewSpecificationBuilder implements SpecificationBuilder<RentalReview,
        ReviewSearchParameters> {
    public static final String CAR_ID = "carId";
    public static final String CAR_MODEL = "model";
    public static final String CAR_BRAND = "brand";
    public static final String CAR_TYPE = "type";

    private final SpecificationProviderManager<RentalReview> reviewSpecificationProviderManager;

    @Override
    public Specification<RentalReview> build(ReviewSearchParameters searchParameters) {
        Specification<RentalReview> spec = Specification.where(null);
        if (searchParameters.carId() != null && !searchParameters.carId().isEmpty()) {
            spec = spec.and(reviewSpecificationProviderManager
                    .getSpecificationProvider(CAR_ID)
                    .getSpecification(searchParameters.carId()));
        }

        if (searchParameters.model() != null && !searchParameters.model().isEmpty()) {
            spec = spec.and(reviewSpecificationProviderManager
                    .getSpecificationProvider(CAR_MODEL)
                    .getSpecification(searchParameters.model()));
        }

        if (searchParameters.brand() != null && !searchParameters.brand().isEmpty()) {
            spec = spec.and(reviewSpecificationProviderManager
                    .getSpecificationProvider(CAR_BRAND)
                    .getSpecification(searchParameters.brand()));
        }

        if (searchParameters.type() != null) {
            String value = searchParameters.type().name();
            spec = spec.and(reviewSpecificationProviderManager
                    .getSpecificationProvider(CAR_TYPE)
                    .getSpecification(value));
        }
        return spec;
    }
}
