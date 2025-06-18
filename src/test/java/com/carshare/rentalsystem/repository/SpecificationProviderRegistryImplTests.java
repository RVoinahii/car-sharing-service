package com.carshare.rentalsystem.repository;

import static com.carshare.rentalsystem.repository.car.spec.CarBrandSpecificationProvider.FIELD_CAR_BRAND;
import static com.carshare.rentalsystem.repository.car.spec.CarModelSpecificationProvider.FIELD_CAR_MODEL;
import static com.carshare.rentalsystem.repository.payment.spec.PaymentStatusSpecificationProvider.FIELD_STATUS;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.exception.SpecificationNotFoundException;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Payment;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.RentalReview;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpecificationProviderRegistryImplTests {
    @Mock
    private SpecificationProvider<Car> carBrandSpecProvider;

    @Mock
    private SpecificationProvider<Payment> paymentStatusSpecProvider;

    @Mock
    private SpecificationProvider<Rental> rentalStatusSpecProvider;

    @Mock
    private SpecificationProvider<RentalReview> reviewCarModelSpecProvider;

    private SpecificationProviderRegistryImpl registry;

    @BeforeEach
    void setUp() {
        List<SpecificationProvider<?>> providers = List.of(
                carBrandSpecProvider,
                paymentStatusSpecProvider,
                rentalStatusSpecProvider,
                reviewCarModelSpecProvider
        );

        registry = new SpecificationProviderRegistryImpl(providers);
        registry.init();
    }

    @Test
    @DisplayName("""
    getSpecificationProvider():
     Should return correct SpecificationProvider for existing provider and entity
            """)
    void getSpecificationProvider_existingProvider_returnsProvider() {
        //Given
        when(carBrandSpecProvider.getKey()).thenReturn(FIELD_CAR_BRAND);
        doReturn(Car.class).when(carBrandSpecProvider).getTargetType();

        when(paymentStatusSpecProvider.getKey()).thenReturn(FIELD_STATUS);
        doReturn(Payment.class).when(paymentStatusSpecProvider).getTargetType();

        when(rentalStatusSpecProvider.getKey()).thenReturn(FIELD_STATUS);
        doReturn(Rental.class).when(rentalStatusSpecProvider).getTargetType();

        when(reviewCarModelSpecProvider.getKey()).thenReturn(FIELD_CAR_MODEL);
        doReturn(RentalReview.class).when(reviewCarModelSpecProvider).getTargetType();

        registry.init();

        //When & Then
        SpecificationProvider<Car> provider = registry
                .getSpecificationProvider(Car.class, FIELD_CAR_BRAND);
        assertSame(carBrandSpecProvider, provider);

        SpecificationProvider<Payment> paymentProvider = registry
                .getSpecificationProvider(Payment.class, FIELD_STATUS);
        assertSame(paymentStatusSpecProvider, paymentProvider);

        SpecificationProvider<Rental> rentalProvider = registry
                .getSpecificationProvider(Rental.class, FIELD_STATUS);
        assertSame(rentalStatusSpecProvider, rentalProvider);

        SpecificationProvider<RentalReview> reviewProvider = registry
                .getSpecificationProvider(RentalReview.class, FIELD_CAR_MODEL);
        assertSame(reviewCarModelSpecProvider, reviewProvider);
    }

    @Test
    @DisplayName("""
    getSpecificationProvider():
     Should throw SpecificationNotFoundException when key does not exist
            """)
    void getSpecificationProvider_nonExistingKey_throwsException() {
        //Given
        when(carBrandSpecProvider.getKey())
                .thenReturn(FIELD_CAR_BRAND);
        doReturn(Car.class).when(carBrandSpecProvider).getTargetType();

        registry.init();

        String nonExistentKey = "nonExistentKey";

        //When & Then
        SpecificationNotFoundException ex = assertThrows(
                SpecificationNotFoundException.class, () -> {
                    registry.getSpecificationProvider(Car.class, nonExistentKey);
                });
        assertTrue(ex.getMessage().contains("No provider for Car and key " + nonExistentKey));
    }

    @Test
    @DisplayName("""
    getSpecificationProvider():
     Should throw SpecificationNotFoundException when entity type does not exist
            """)
    void getSpecificationProvider_nonExistingEntity_throwsException() {
        //Given
        when(carBrandSpecProvider.getKey())
                .thenReturn(FIELD_CAR_BRAND);
        doReturn(Car.class).when(carBrandSpecProvider).getTargetType();
        registry.init();

        String anyKey = "anyKey";

        //When & Then
        SpecificationNotFoundException ex = assertThrows(
                SpecificationNotFoundException.class, () -> {
                    registry.getSpecificationProvider(String.class, anyKey);
                });
        assertTrue(ex.getMessage().contains("No provider for String and key " + anyKey));
    }
}
