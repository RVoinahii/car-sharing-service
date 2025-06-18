package com.carshare.rentalsystem.repository;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SpecificationBuilderFactory {
    private final SpecificationProviderRegistryImpl registry;

    public <T> SpecificationBuilder<T, Map<String, String>> getBuilder(Class<T> entityClass) {
        return new GlobalSpecificationBuilder<>(registry, entityClass);
    }
}
