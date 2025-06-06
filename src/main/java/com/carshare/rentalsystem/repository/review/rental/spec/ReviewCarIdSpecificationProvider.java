package com.carshare.rentalsystem.repository.review.rental.spec;

import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_ID;
import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_RENTAL;
import static com.carshare.rentalsystem.repository.review.rental.spec.ReviewCarBrandSpecificationProvider.FIELD_CAR;

import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import com.carshare.rentalsystem.repository.review.rental.ReviewSpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ReviewCarIdSpecificationProvider implements SpecificationProvider<RentalReview> {
    @Override
    public String getKey() {
        return ReviewSpecificationBuilder.FIELD_CAR_ID;
    }

    @Override
    public Specification<RentalReview> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(FIELD_RENTAL).get(FIELD_CAR).get(FIELD_ID), params);
    }
}
