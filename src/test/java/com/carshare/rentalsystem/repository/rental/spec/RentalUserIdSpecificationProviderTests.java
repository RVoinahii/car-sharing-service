package com.carshare.rentalsystem.repository.rental.spec;

import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_ID;
import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_USER;
import static com.carshare.rentalsystem.repository.payment.spec.PaymentUserIdSpecificationProvider.FIELD_USER_ID;
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
public class RentalUserIdSpecificationProviderTests
        extends AbstractSpecificationProviderTests<Rental> {
    @InjectMocks
    private RentalUserIdSpecificationProvider userIdSpecificationProvider;

    @Mock
    private Root<Rental> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> userPath;

    @Mock
    private Path<Long> userIdPath;

    @Mock
    private Predicate excpectedPredicate;

    @Override
    protected SpecificationProvider<Rental> createProvider() {
        return userIdSpecificationProvider;
    }

    @Override
    protected String expectedKey() {
        return FIELD_USER_ID;
    }

    @Override
    protected Class<Rental> expectedTargetType() {
        return Rental.class;
    }

    @Test
    @DisplayName("getSpecification(): Should return Predicate matching user ID")
    void getSpecification_returnsCorrectPredicate() {
        //Given
        String param = "123";
        Long userId = Long.valueOf(param);

        when(root.get(FIELD_USER)).thenReturn(userPath);
        when((Path) userPath.get(FIELD_ID)).thenReturn(userIdPath);
        when(criteriaBuilder.equal(userIdPath, userId)).thenReturn(excpectedPredicate);

        //When
        Specification<Rental> specification = userIdSpecificationProvider.getSpecification(param);
        Predicate actualPredicate = specification.toPredicate(root, query, criteriaBuilder);

        // hen
        assertNotNull(actualPredicate);
        assertEquals(excpectedPredicate, actualPredicate);
    }
}
