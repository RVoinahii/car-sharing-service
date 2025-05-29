package com.carshare.rentalsystem.repository.payment;

import com.carshare.rentalsystem.exception.SpecificationNotFoundException;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentSpecificationProviderManager implements SpecificationProviderManager<Payment> {
    private final List<SpecificationProvider<Payment>> rentalSpecificationProviders;

    @Override
    public SpecificationProvider<Payment> getSpecificationProvider(String key) {
        return rentalSpecificationProviders.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new SpecificationNotFoundException(
                        "Can't find correct specification provided for key " + key));
    }
}
