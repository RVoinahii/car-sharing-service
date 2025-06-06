package com.carshare.rentalsystem.repository.rental;

import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationBuilder;
import com.carshare.rentalsystem.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationBuilder implements SpecificationBuilder<Rental,
        RentalSearchParameters> {
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_RENTAL_STATUS = "status";

    private final SpecificationProviderManager<Rental> rentalSpecificationProviderManager;

    @Override
    public Specification<Rental> build(RentalSearchParameters searchParameters) {
        Specification<Rental> spec = Specification.where(null);
        if (searchParameters.userId() != null && !searchParameters.userId().isEmpty()) {
            spec = spec.and(rentalSpecificationProviderManager
                    .getSpecificationProvider(FIELD_USER_ID)
                    .getSpecification(searchParameters.userId()));
        }

        if (searchParameters.status() != null) {
            String value = searchParameters.status().name();
            spec = spec.and(rentalSpecificationProviderManager
                    .getSpecificationProvider(FIELD_RENTAL_STATUS)
                    .getSpecification(value));
        }
        return spec;
    }
}
