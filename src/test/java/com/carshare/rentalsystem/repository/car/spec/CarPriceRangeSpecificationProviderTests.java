package com.carshare.rentalsystem.repository.car.spec;

import static com.carshare.rentalsystem.repository.car.spec.CarPriceRangeSpecificationProvider.FIELD_CAR_DAILY_FEE;
import static com.carshare.rentalsystem.repository.car.spec.CarPriceRangeSpecificationProvider.PRICE_RANGE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.exception.InvalidPriceRangeFormatException;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.AbstractSpecificationProviderTests;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class CarPriceRangeSpecificationProviderTests
        extends AbstractSpecificationProviderTests<Car> {
    @InjectMocks
    private CarPriceRangeSpecificationProvider priceRangeSpecificationProvider;

    @Mock
    private Root<Car> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<BigDecimal> dayliFeePath;

    @Mock
    private Predicate excpectedPredicate;

    @Override
    protected SpecificationProvider<Car> createProvider() {
        return priceRangeSpecificationProvider;
    }

    @Override
    protected String expectedKey() {
        return PRICE_RANGE_KEY;
    }

    @Override
    protected Class<Car> expectedTargetType() {
        return Car.class;
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate for range with both bounds (bottomPrice-upperPrice)
            """)
    void getSpecification_fullRange_returnsCorrectPredicate() {
        //Given
        String param = "10-100";
        BigDecimal bottom = new BigDecimal("10");
        BigDecimal upper = new BigDecimal("100");

        when((Path) root.get(FIELD_CAR_DAILY_FEE)).thenReturn(dayliFeePath);
        when(criteriaBuilder.between(dayliFeePath, bottom, upper)).thenReturn(excpectedPredicate);

        //When
        Specification<Car> specification = priceRangeSpecificationProvider.getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        //Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate for bottom bound only (bottomPrice-null)
            """)
    void getSpecification_bottomOnly_returnsCorrectPredicate() {
        //Given
        String param = "10-null";
        BigDecimal bottom = new BigDecimal("10");

        when((Path) root.get(FIELD_CAR_DAILY_FEE)).thenReturn(dayliFeePath);
        when(criteriaBuilder.greaterThanOrEqualTo(dayliFeePath, bottom))
                .thenReturn(excpectedPredicate);

        //When
        Specification<Car> specification = priceRangeSpecificationProvider.getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        //Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate for upper bound only (null-upperPrice)
            """)
    void getSpecification_upperOnly_returnsCorrectPredicate() {
        //Given
        String param = "null-100";
        BigDecimal upper = new BigDecimal("100");

        when((Path) root.get(FIELD_CAR_DAILY_FEE)).thenReturn(dayliFeePath);
        when(criteriaBuilder.lessThanOrEqualTo(dayliFeePath, upper))
                .thenReturn(excpectedPredicate);

        //When
        Specification<Car> specification = priceRangeSpecificationProvider.getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        //Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should throw exception for invalid format (missing bound)
            """)
    void getSpecification_invalidFormat_shouldThrowException() {
        //Given
        String param = "10";

        //When & Then
        InvalidPriceRangeFormatException exception = assertThrows(
                InvalidPriceRangeFormatException.class, () -> {
                    priceRangeSpecificationProvider.getSpecification(param);
                });

        assertTrue(exception.getMessage().contains("Price range must be in format"));
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should throw exception for invalid number format
            """)
    void getSpecification_invalidNumberFormat_shouldThrowException() {
        //Given
        String param = "10-abc";

        //When & Then
        InvalidPriceRangeFormatException exception = assertThrows(
                InvalidPriceRangeFormatException.class, () -> {
                    priceRangeSpecificationProvider.getSpecification(param);
                });

        assertTrue(exception.getMessage().contains("Invalid price range"));
    }
}
