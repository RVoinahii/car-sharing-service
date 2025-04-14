package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder.IS_ACTIVE;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IsActiveSpecificationProvider implements SpecificationProvider<Rental> {
    private static final String STRING_BOOLEAN_TRUE = "true";
    private static final String RENTAL_ACTUAL_RETURN_DATE = "actualReturnDate";

    @Override
    public String getKey() {
        return IS_ACTIVE;
    }

    @Override
    public Specification<Rental> getSpecification(String params) {
        boolean isActive = STRING_BOOLEAN_TRUE.equalsIgnoreCase(params);
        return (root, query, criteriaBuilder) -> {
            if (isActive) {
                return criteriaBuilder.isNull(root.get(RENTAL_ACTUAL_RETURN_DATE));
            } else {
                return criteriaBuilder.isNotNull(root.get(RENTAL_ACTUAL_RETURN_DATE));
            }
        };
    }
}
