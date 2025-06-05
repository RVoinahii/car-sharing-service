package com.carshare.rentalsystem.repository.review.rental;

import com.carshare.rentalsystem.exception.SpecificationNotFoundException;
import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewSpecificationProviderManager
        implements SpecificationProviderManager<RentalReview> {
    private final List<SpecificationProvider<RentalReview>> specificationProviders;

    @Override
    public SpecificationProvider<RentalReview> getSpecificationProvider(String key) {
        return specificationProviders.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new SpecificationNotFoundException(
                        "Can't find correct specification provided for key " + key));
    }
}
