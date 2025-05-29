package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder.USER_ID;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class RentalUserIdSpecificationProvider implements SpecificationProvider<Rental> {

    @Override
    public String getKey() {
        return USER_ID;
    }

    @Override
    public Specification<Rental> getSpecification(String params) {
        return (root, query, criteriaBuilder) -> {
            Path<Long> userIdPath = root.get("user").get("id");
            return criteriaBuilder.equal(userIdPath, Long.valueOf(params));
        };
    }
}
