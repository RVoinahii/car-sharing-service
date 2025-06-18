package com.carshare.rentalsystem.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractSpecificationProviderTests<T> {
    protected SpecificationProvider<T> specificationProvider;

    protected abstract SpecificationProvider<T> createProvider();

    protected abstract String expectedKey();

    protected abstract Class<T> expectedTargetType();

    @BeforeEach
    void setUp() {
        this.specificationProvider = createProvider();
    }

    @Test
    @DisplayName("getKey(): Should return correct key")
    void getKey_returnsCorrectKey() {
        //When
        String actualKey = specificationProvider.getKey();

        //Then
        assertEquals(expectedKey(), actualKey);
    }

    @Test
    @DisplayName("getTargetType(): Should return correct target type")
    void getTargetType_returnsCorrectTargetType() {
        //When
        Class<?> targetType = specificationProvider.getTargetType();

        //Then
        assertEquals(expectedTargetType(), targetType);
    }
}
