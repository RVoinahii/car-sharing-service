package com.carshare.rentalsystem.repository.car.spec;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarOnlyAvailableSpecificationProvider implements SpecificationProvider<Car> {
    public static final String ONLY_AVAILABLE_KEY = "onlyAvailable";
    public static final String FIELD_CAR_INVENTORY = "inventory";

    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_FALSE = "false";

    @Override
    public String getKey() {
        return ONLY_AVAILABLE_KEY;
    }

    @Override
    public Specification<Car> getSpecification(String params) {
        if (!BOOLEAN_TRUE.equalsIgnoreCase(params) && !BOOLEAN_FALSE.equalsIgnoreCase(params)) {
            throw new IllegalArgumentException("Invalid value for onlyAvailable: " + params);
        }

        boolean onlyAvailable = Boolean.parseBoolean(params);

        return (root, query, criteriaBuilder) -> {
            if (onlyAvailable) {
                return criteriaBuilder.greaterThan(root.get(FIELD_CAR_INVENTORY), 0);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_CAR_INVENTORY), 0);
            }
        };
    }

    @Override
    public Class<?> getTargetType() {
        return Car.class;
    }
}
