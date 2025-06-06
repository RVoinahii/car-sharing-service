package com.carshare.rentalsystem.repository.review.rental.spec;

import static com.carshare.rentalsystem.repository.review.rental.ReviewSpecificationBuilder.FIELD_CAR_TYPE;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ReviewCarTypeSpecificationProvider implements SpecificationProvider<RentalReview> {
    @Override
    public String getKey() {
        return FIELD_CAR_TYPE;
    }

    @Override
    public Specification<RentalReview> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get(FIELD_CAR_TYPE),
                        Car.Type.valueOf(params.toUpperCase())
                );
    }
}
