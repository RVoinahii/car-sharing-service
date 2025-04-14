package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder.USER_ID;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserIdSpecificationProvider implements SpecificationProvider<Rental> {

    @Override
    public String getKey() {
        return USER_ID;
    }

    @Override
    public Specification<Rental> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                root.get(USER_ID).in(params);
    }
}
