package com.carshare.rentalsystem.repository.rental;

import com.carshare.rentalsystem.dto.rental.RentalSearchParameters;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationBuilder;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationBuilder implements SpecificationBuilder<Rental> {
    public static final String USER_ID = "userId";
    public static final String IS_ACTIVE = "isActive";

    private final SpecificationProviderManager<Rental> rentalSpecificationProviderManager;

    @Override
    public Specification<Rental> build(RentalSearchParameters searchParameters) {
        Specification<Rental> spec = Specification.where(null);
        if (searchParameters.userId() != null && !searchParameters.userId().isEmpty()) {
            spec = spec.and(rentalSpecificationProviderManager
                    .getSpecificationProvider(USER_ID)
                    .getSpecification(searchParameters.userId()));
        }

        if (searchParameters.isActive() != null) {
            String value = String.valueOf(searchParameters.isActive());
            spec = spec.and(rentalSpecificationProviderManager
                    .getSpecificationProvider(IS_ACTIVE)
                    .getSpecification(value));
        }
        return spec;
    }
}
