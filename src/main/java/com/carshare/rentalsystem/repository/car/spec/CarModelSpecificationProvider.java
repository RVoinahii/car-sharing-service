package com.carshare.rentalsystem.repository.car.spec;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarModelSpecificationProvider implements SpecificationProvider<Car> {
    public static final String FIELD_CAR_MODEL = "model";

    @Override
    public String getKey() {
        return FIELD_CAR_MODEL;
    }

    @Override
    public Specification<Car> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get(FIELD_CAR_MODEL)), "%" + params.toLowerCase() + "%"
                );
    }

    @Override
    public Class<?> getTargetType() {
        return Car.class;
    }
}
