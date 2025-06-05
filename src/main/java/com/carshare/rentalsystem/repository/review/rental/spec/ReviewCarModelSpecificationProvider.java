package com.carshare.rentalsystem.repository.review.rental.spec;

import static com.carshare.rentalsystem.repository.review.rental.ReviewSpecificationBuilder.CAR_MODEL;

import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ReviewCarModelSpecificationProvider implements SpecificationProvider<RentalReview> {
    @Override
    public String getKey() {
        return CAR_MODEL;
    }

    @Override
    public Specification<RentalReview> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("rental").get("car").get("model")),
                        "%" + params.toLowerCase() + "%");
    }
}
