package com.carshare.rentalsystem.repository.car.spec;

import static com.carshare.rentalsystem.repository.car.spec.CarBrandSpecificationProvider.FIELD_CAR_BRAND;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.CAR_BRAND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
public class CarBrandSpecificationProviderTests extends AbstractSpecificationProviderTests<Car> {
    @InjectMocks
    private CarBrandSpecificationProvider brandSpecificationProvider;

    @Mock
    private Root<Car> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<String> brandPath;

    @Mock
    private Predicate excpectedPredicate;

    @Override
    protected SpecificationProvider<Car> createProvider() {
        return brandSpecificationProvider;
    }

    @Override
    protected String expectedKey() {
        return FIELD_CAR_BRAND;
    }

    @Override
    protected Class<Car> expectedTargetType() {
        return Car.class;
    }

    @Test
    @DisplayName("""
            getSpecification():
             Should return the correct Predicate for the brand filter
            """)
    void getSpecification_returnsCorrectPredicate() {
        //Given
        String brand = CAR_BRAND;

        when(root.<String>get(FIELD_CAR_BRAND)).thenReturn(brandPath);
        when(criteriaBuilder.lower(brandPath)).thenReturn(brandPath);
        when(criteriaBuilder.like(eq(brandPath), anyString()
                .toLowerCase())).thenReturn(excpectedPredicate);

        //When
        Specification<Car> carSpecification =
                brandSpecificationProvider.getSpecification(brand);
        Predicate actualPredicate = carSpecification.toPredicate(root, query, criteriaBuilder);

        //Then
        assertNotNull(actualPredicate);
        assertThat(actualPredicate).isEqualTo(excpectedPredicate);
    }
}
