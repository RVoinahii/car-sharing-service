package com.carshare.rentalsystem.repository.payment.spec;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusSpecificationProvider implements SpecificationProvider<Payment> {
    public static final String FIELD_STATUS = "status";

    @Override
    public String getKey() {
        return FIELD_STATUS;
    }

    @Override
    public Specification<Payment> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get(FIELD_STATUS),
                        Payment.PaymentStatus.valueOf(params.toUpperCase())
                );
    }

    @Override
    public Class<?> getTargetType() {
        return Payment.class;
    }
}
