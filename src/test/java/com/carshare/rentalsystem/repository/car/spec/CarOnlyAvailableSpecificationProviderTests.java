package com.carshare.rentalsystem.repository.car.spec;

import static com.carshare.rentalsystem.repository.car.spec.CarOnlyAvailableSpecificationProvider.FIELD_CAR_INVENTORY;
import static com.carshare.rentalsystem.repository.car.spec.CarOnlyAvailableSpecificationProvider.ONLY_AVAILABLE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.AbstractSpecificationProviderTests;
import com.carshare.rentalsystem.repository.SpecificationProvider;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class CarOnlyAvailableSpecificationProviderTests
        extends AbstractSpecificationProviderTests<Car> {
    @InjectMocks
    private CarOnlyAvailableSpecificationProvider onlyAvailableSpecificationProvider;

    @Mock
    private Root<Car> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Integer> inventoryPath;

    @Mock
    private Predicate excpectedPredicate;

    @Override
    protected SpecificationProvider<Car> createProvider() {
        return onlyAvailableSpecificationProvider;
    }

    @Override
    protected String expectedKey() {
        return ONLY_AVAILABLE_KEY;
    }

    @Override
    protected Class<Car> expectedTargetType() {
        return Car.class;
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate for onlyAvailable = true (inventory > 0)
            """)
    void getSpecification_onlyAvailableTrue_returnsCorrectPredicate() {
        // Given
        String param = "true";

        when(root.<Integer>get(FIELD_CAR_INVENTORY)).thenReturn(inventoryPath);
        when(criteriaBuilder.greaterThan(inventoryPath, 0)).thenReturn(excpectedPredicate);

        // When
        Specification<Car> specification = onlyAvailableSpecificationProvider
                .getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        // Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate for onlyAvailable = false (inventory <= 0)
            """)
    void getSpecification_onlyAvailableFalse_returnsCorrectPredicate() {
        // Given
        String param = "false";

        when(root.<Integer>get(FIELD_CAR_INVENTORY)).thenReturn(inventoryPath);
        when(criteriaBuilder.lessThanOrEqualTo(inventoryPath, 0)).thenReturn(excpectedPredicate);

        // When
        Specification<Car> specification = onlyAvailableSpecificationProvider
                .getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        // Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should throw exception for invalid param
            """)
    void getSpecification_invalidParam_shouldThrowException() {
        // Given
        String invalidParam = "notABoolean";

        // When / Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            onlyAvailableSpecificationProvider.getSpecification(invalidParam);
        });

        assertTrue(exception.getMessage().contains("Invalid value for onlyAvailable"));
    }
}
