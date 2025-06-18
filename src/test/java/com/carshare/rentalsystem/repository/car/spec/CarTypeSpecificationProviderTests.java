package com.carshare.rentalsystem.repository.car.spec;

import static com.carshare.rentalsystem.repository.car.spec.CarTypeSpecificationProvider.FIELD_CAR_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class CarTypeSpecificationProviderTests extends AbstractSpecificationProviderTests<Car> {
    @InjectMocks
    private CarTypeSpecificationProvider typeSpecificationProvider;

    @Mock
    private Root<Car> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Car.Type> typePath;

    @Mock
    private Predicate excpectedPredicate;

    @Override
    protected SpecificationProvider<Car> createProvider() {
        return typeSpecificationProvider;
    }

    @Override
    protected String expectedKey() {
        return FIELD_CAR_TYPE;
    }

    @Override
    protected Class<Car> expectedTargetType() {
        return Car.class;
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate matching car type (case insensitive)
            """)
    void getSpecification_returnsCorrectPredicate() {
        //Given
        String param = "sedan";
        Car.Type expectedType = Car.Type.valueOf(param.toUpperCase());

        when((Path) root.get(CarTypeSpecificationProvider.FIELD_CAR_TYPE)).thenReturn(typePath);
        when(criteriaBuilder.equal(typePath, expectedType)).thenReturn(excpectedPredicate);

        //When
        Specification<Car> specification = typeSpecificationProvider.getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        //Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }
}
