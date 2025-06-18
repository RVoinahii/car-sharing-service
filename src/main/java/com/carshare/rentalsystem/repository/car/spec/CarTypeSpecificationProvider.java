package com.carshare.rentalsystem.repository.car.spec;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarTypeSpecificationProvider implements SpecificationProvider<Car> {
    public static final String FIELD_CAR_TYPE = "type";

    @Override
    public String getKey() {
        return FIELD_CAR_TYPE;
    }

    @Override
    public Specification<Car> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get(FIELD_CAR_TYPE), Car.Type.valueOf(params.toUpperCase())
                );
    }

    @Override
    public Class<?> getTargetType() {
        return Car.class;
    }
}
