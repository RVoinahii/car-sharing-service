package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder.FIELD_RENTAL_STATUS;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class RentalStatusSpecificationProvider implements SpecificationProvider<Rental> {

    @Override
    public String getKey() {
        return FIELD_RENTAL_STATUS;
    }

    @Override
    public Specification<Rental> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get(FIELD_RENTAL_STATUS),
                        Rental.RentalStatus.valueOf(params.toUpperCase())
                );
    }
}
