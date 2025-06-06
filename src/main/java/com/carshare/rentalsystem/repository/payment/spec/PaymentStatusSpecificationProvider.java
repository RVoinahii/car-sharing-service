package com.carshare.rentalsystem.repository.payment.spec;

import static com.carshare.rentalsystem.repository.payment.PaymentSpecificationBuilder.FIELD_PAYMENT_STATUS;

import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusSpecificationProvider implements SpecificationProvider<Payment> {
    @Override
    public String getKey() {
        return FIELD_PAYMENT_STATUS;
    }

    @Override
    public Specification<Payment> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get(FIELD_PAYMENT_STATUS),
                        Payment.PaymentStatus.valueOf(params.toUpperCase())
                );
    }
}
