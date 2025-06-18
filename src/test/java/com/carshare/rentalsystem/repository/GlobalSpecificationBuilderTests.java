package com.carshare.rentalsystem.repository;

import static com.carshare.rentalsystem.repository.car.spec.CarBrandSpecificationProvider.FIELD_CAR_BRAND;
import static com.carshare.rentalsystem.repository.car.spec.CarTypeSpecificationProvider.FIELD_CAR_TYPE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.exception.SpecificationNotFoundException;
import com.carshare.rentalsystem.model.Car;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class GlobalSpecificationBuilderTests {
    @Mock
    private SpecificationProviderRegistryImpl registry;

    @Mock
    private SpecificationProvider<Car> carBrandSpecProvider;

    @Mock
    private SpecificationProvider<Car> carTypeSpecProvider;

    @Mock
    private Specification<Car> brandSpec;

    @Mock
    private Specification<Car> typeSpec;

    private GlobalSpecificationBuilder<Car> builder;

    @BeforeEach
    void setUp() {
        builder = new GlobalSpecificationBuilder<>(registry, Car.class);
    }

    @Test
    @DisplayName("""
        build():
         Should build combined Specification when multiple parameters provided
            """)
    void build_multipleParams_shouldBuildCombinedSpecification() {
        //Given
        Map<String, String> params = Map.of(
                FIELD_CAR_BRAND, "Toyota",
                FIELD_CAR_TYPE, "SUV"
        );

        when(registry.getSpecificationProvider(Car.class, FIELD_CAR_BRAND))
                .thenReturn(carBrandSpecProvider);
        when(registry.getSpecificationProvider(Car.class, FIELD_CAR_TYPE))
                .thenReturn(carTypeSpecProvider);

        when(carBrandSpecProvider.getSpecification("Toyota"))
                .thenReturn(brandSpec);
        when(carTypeSpecProvider.getSpecification("SUV"))
                .thenReturn(typeSpec);

        //When
        Specification<Car> result = builder.build(params);

        //Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("""
        build():
         Should return empty Specification when no parameters provided
            """)
    void build_noParams_shouldReturnEmptySpecification() {
        //Given
        Map<String, String> params = Map.of();

        //When
        Specification<Car> result = builder.build(params);

        //Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("""
        build():
         Should throw SpecificationNotFoundException when provider not found
            """)
    void build_providerNotFound_shouldThrowException() {
        //Given
        Map<String, String> params = Map.of("unknownKey", "someValue");

        when(registry.getSpecificationProvider(Car.class, "unknownKey"))
                .thenThrow(new SpecificationNotFoundException("No provider"));

        //When & Then
        SpecificationNotFoundException ex = assertThrows(
                SpecificationNotFoundException.class,
                () -> builder.build(params)
        );

        assertTrue(ex.getMessage().contains("No provider"));
    }
}
