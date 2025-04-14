package com.carshare.rentalsystem.repository.rental;

import com.carshare.rentalsystem.exception.SpecificationNotFoundException;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationProviderManager implements SpecificationProviderManager<Rental> {
    private final List<SpecificationProvider<Rental>> rentalSpecificationProviders;

    @Override
    public SpecificationProvider<Rental> getSpecificationProvider(String key) {
        return rentalSpecificationProviders.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new SpecificationNotFoundException(
                        "Can't find correct specification provided for key " + key));
    }
}
