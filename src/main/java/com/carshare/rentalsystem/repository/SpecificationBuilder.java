package com.carshare.rentalsystem.repository;

import com.carshare.rentalsystem.dto.rental.response.dto.RentalSearchParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(RentalSearchParameters params);
}
