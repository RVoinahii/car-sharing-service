package com.carshare.rentalsystem.repository.payment.spec;

import static com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder.USER_ID;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PaymentUserIdSpecificationProvider implements SpecificationProvider<Payment> {
    @Override
    public String getKey() {
        return USER_ID;
    }

    @Override
    public Specification<Payment> getSpecification(String params) {
        return (root, query, criteriaBuilder) -> {
            Path<Long> userIdPath = root.get("rental").get("user").get("id");
            return criteriaBuilder.equal(userIdPath, Long.valueOf(params));
        };
    }
}
