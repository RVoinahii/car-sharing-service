package com.carshare.rentalsystem.repository;

import com.carshare.rentalsystem.exception.SpecificationNotFoundException;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SpecificationProviderRegistryImpl implements SpecificationProviderRegistry {
    private final List<SpecificationProvider<?>> allProviders;
    private final Map<Class<?>, Map<String, SpecificationProvider<?>>> registry = new HashMap<>();

    @PostConstruct
    void init() {
        for (SpecificationProvider<?> provider : allProviders) {
            Class<?> entityType = provider.getTargetType();
            registry
                    .computeIfAbsent(entityType, k -> new HashMap<>())
                    .put(provider.getKey(), provider);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> SpecificationProvider<T> getSpecificationProvider(Class<T> entityType, String key) {
        Map<String, SpecificationProvider<?>> providers = registry.get(entityType);
        if (providers == null || !providers.containsKey(key)) {
            throw new SpecificationNotFoundException("No provider for "
                    + entityType.getSimpleName() + " and key " + key);
        }
        return (SpecificationProvider<T>) providers.get(key);
    }
}
