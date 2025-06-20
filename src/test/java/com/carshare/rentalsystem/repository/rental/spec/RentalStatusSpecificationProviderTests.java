package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.payment.spec.PaymentStatusSpecificationProvider.FIELD_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.model.Rental;
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
public class RentalStatusSpecificationProviderTests
        extends AbstractSpecificationProviderTests<Rental> {
    @InjectMocks
    private RentalStatusSpecificationProvider statusSpecificationProvider;

    @Mock
    private Root<Rental> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Rental.RentalStatus> statusPath;

    @Mock
    private Predicate excpectedPredicate;

    @Override
    protected SpecificationProvider<Rental> createProvider() {
        return statusSpecificationProvider;
    }

    @Override
    protected String expectedKey() {
        return FIELD_STATUS;
    }

    @Override
    protected Class<Rental> expectedTargetType() {
        return Rental.class;
    }

    @Test
    @DisplayName("""
        getSpecification():
         Should return Predicate matching car type (case insensitive)
            """)
    void getSpecification_returnsCorrectPredicate() {
        //Given
        String param = "reserved";
        Rental.RentalStatus expectedType = Rental.RentalStatus.valueOf(param.toUpperCase());

        when((Path) root.get(FIELD_STATUS)).thenReturn(statusPath);
        when(criteriaBuilder.equal(statusPath, expectedType)).thenReturn(excpectedPredicate);

        //When
        Specification<Rental> specification = statusSpecificationProvider.getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        //Then
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }
}
