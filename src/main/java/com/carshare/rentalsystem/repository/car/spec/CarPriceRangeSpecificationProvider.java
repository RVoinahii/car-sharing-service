package com.carshare.rentalsystem.repository.car.spec;

import com.carshare.rentalsystem.exception.InvalidPriceRangeFormatException;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CarPriceRangeSpecificationProvider implements SpecificationProvider<Car> {
    public static final String PRICE_RANGE_KEY = "priceRange";
    public static final String FIELD_CAR_DAILY_FEE = "dailyFee";

    private static final String DELIMITER = "-";
    private static final int BOUNDS_EXPECTED_LENGTH = 2;
    private static final int BOTTOM_PRICE_PART = 0;
    private static final int UPPER_PRICE_PART = 1;

    @Override
    public String getKey() {
        return PRICE_RANGE_KEY;
    }

    @Override
    public Specification<Car> getSpecification(String params) {
        String[] bounds = params.trim().split(DELIMITER);
        if (bounds.length != BOUNDS_EXPECTED_LENGTH) {
            throw new InvalidPriceRangeFormatException(
                    "Price range must be in format 'bottomPrice-upperPrice', but got: " + params);
        }

        BigDecimal bottomPrice = parseBound(bounds[BOTTOM_PRICE_PART].trim());
        BigDecimal upperPrice = parseBound(bounds[UPPER_PRICE_PART].trim());

        return (root, query, cb) -> {
            if (bottomPrice != null && upperPrice != null) {
                return cb.between(root.get(FIELD_CAR_DAILY_FEE), bottomPrice, upperPrice);
            } else if (bottomPrice != null) {
                return cb.greaterThanOrEqualTo(root.get(FIELD_CAR_DAILY_FEE), bottomPrice);
            } else {
                return cb.lessThanOrEqualTo(root.get(FIELD_CAR_DAILY_FEE), upperPrice);
            }
        };
    }

    @Override
    public Class<?> getTargetType() {
        return Car.class;
    }

    private BigDecimal parseBound(String str) {
        try {
            return "null".equalsIgnoreCase(str) ? null : new BigDecimal(str);
        } catch (NumberFormatException e) {
            throw new InvalidPriceRangeFormatException("Invalid price range: " + str);
        }
    }
}
