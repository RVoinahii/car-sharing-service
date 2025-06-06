package com.carshare.rentalsystem.repository.review.rental.spec;

import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_RENTAL;
import static com.carshare.rentalsystem.repository.review.rental.ReviewSpecificationBuilder.FIELD_CAR_BRAND;

import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ReviewCarBrandSpecificationProvider implements SpecificationProvider<RentalReview> {
    public static final String FIELD_CAR = "car";

    @Override
    public String getKey() {
        return FIELD_CAR_BRAND;
    }

    @Override
    public Specification<RentalReview> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(FIELD_RENTAL).get(FIELD_CAR)
                                .get(FIELD_CAR_BRAND)), "%" + params.toLowerCase() + "%");
    }
}
