package com.carshare.rentalsystem.repository.payment;

import static com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder.USER_ID;

import com.carshare.rentalsystem.dto.payment.request.dto.PaymentSearchParameters;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.repository.SpecificationBuilder;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentSpecificationBuilder implements SpecificationBuilder<Payment,
        PaymentSearchParameters> {
    public static final String PAYMENT_STATUS = "status";

    private final SpecificationProviderManager<Payment> paymentSpecificationProviderManager;

    @Override
    public Specification<Payment> build(PaymentSearchParameters searchParameters) {
        Specification<Payment> spec = Specification.where(null);
        if (searchParameters.userId() != null && !searchParameters.userId().isEmpty()) {
            spec = spec.and(paymentSpecificationProviderManager
                    .getSpecificationProvider(USER_ID)
                    .getSpecification(searchParameters.userId()));
        }

        if (searchParameters.status() != null) {
            String value = searchParameters.status().name();
            spec = spec.and(paymentSpecificationProviderManager
                    .getSpecificationProvider(PAYMENT_STATUS)
                    .getSpecification(value));
        }
        return spec;
    }
}
