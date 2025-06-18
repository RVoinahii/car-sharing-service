package com.carshare.rentalsystem.repository.car.spec;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarBrandSpecificationProvider implements SpecificationProvider<Car> {
    public static final String FIELD_CAR_BRAND = "brand";

    @Override
    public String getKey() {
        return FIELD_CAR_BRAND;
    }

    @Override
    public Specification<Car> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(
                        FIELD_CAR_BRAND)), "%" + params.toLowerCase() + "%"
                );
    }

    @Override
    public Class<?> getTargetType() {
        return Car.class;
    }
}
