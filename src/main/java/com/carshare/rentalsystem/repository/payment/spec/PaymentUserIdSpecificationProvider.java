package com.carshare.rentalsystem.repository.payment.spec;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PaymentUserIdSpecificationProvider implements SpecificationProvider<Payment> {
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_RENTAL = "rental";
    public static final String FIELD_USER = "user";
    public static final String FIELD_ID = "id";

    @Override
    public String getKey() {
        return FIELD_USER_ID;
    }

    @Override
    public Specification<Payment> getSpecification(String params) {
        return (root, query, criteriaBuilder) -> {
            Path<Long> userIdPath = root.get(FIELD_RENTAL).get(FIELD_USER).get(FIELD_ID);
            return criteriaBuilder.equal(userIdPath, Long.valueOf(params));
        };
    }

    @Override
    public Class<?> getTargetType() {
        return Payment.class;
    }
}
