package com.carshare.rentalsystem.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carshare.rentalsystem.model.Car;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpecificationBuilderFactoryTests {
    @Mock
    private SpecificationProviderRegistryImpl registry;

    @InjectMocks
    private SpecificationBuilderFactory factory;

    @Test
    @DisplayName("""
    getBuilder():
     Should return GlobalSpecificationBuilder with correct entityClass
            """)
    void getBuilder_shouldReturnBuilderWithCorrectEntityClass() throws Exception {
        //Given
        Class<Car> entityClass = Car.class;

        //When
        SpecificationBuilder<Car, Map<String, String>> builder = factory.getBuilder(entityClass);

        //Then
        assertNotNull(builder);
        assertTrue(builder instanceof GlobalSpecificationBuilder);

        GlobalSpecificationBuilder<Car> genericBuilder =
                (GlobalSpecificationBuilder<Car>) builder;

        Field entityClassField = GlobalSpecificationBuilder.class.getDeclaredField("entityClass");
        entityClassField.setAccessible(true);
        Class<?> actualEntityClass = (Class<?>) entityClassField.get(genericBuilder);

        assertEquals(entityClass, actualEntityClass);
    }
}
