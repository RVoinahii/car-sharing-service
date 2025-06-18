package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_USER;
import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_USER_ID;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class RentalUserIdSpecificationProvider implements SpecificationProvider<Rental> {
    @Override
    public String getKey() {
        return FIELD_USER_ID;
    }

    @Override
    public Specification<Rental> getSpecification(String params) {
        return (root, query, criteriaBuilder) -> {
            Path<Long> userIdPath = root.get(FIELD_USER).get("id");
            return criteriaBuilder.equal(userIdPath, Long.valueOf(params));
        };
    }

    @Override
    public Class<?> getTargetType() {
        return Rental.class;
    }
}
