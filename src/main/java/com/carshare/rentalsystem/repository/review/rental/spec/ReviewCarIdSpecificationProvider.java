package com.carshare.rentalsystem.repository.review.rental.spec;

import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_ID;
import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_RENTAL;
import static com.carshare.rentalsystem.repository.review.rental.spec.ReviewCarBrandSpecificationProvider.FIELD_CAR;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ReviewCarIdSpecificationProvider implements SpecificationProvider<RentalReview> {
    public static final String FIELD_CAR_ID = "carId";

    @Override
    public String getKey() {
        return FIELD_CAR_ID;
    }

    @Override
    public Specification<RentalReview> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(FIELD_RENTAL).get(FIELD_CAR).get(FIELD_ID), params);
    }

    @Override
    public Class<?> getTargetType() {
        return Rental.class;
    }
}
