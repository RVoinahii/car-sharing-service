package com.carshare.rentalsystem.repository;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class GlobalSpecificationBuilder<T>
        implements SpecificationBuilder<T, Map<String, String>> {
    private final SpecificationProviderRegistryImpl registry;
    private final Class<T> entityClass;

    @Override
    public Specification<T> build(Map<String, String> params) {
        Specification<T> spec = Specification.where(null);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            SpecificationProvider<T> provider = registry.getSpecificationProvider(
                    entityClass, entry.getKey());
            spec = spec.and(provider.getSpecification(entry.getValue()));
        }

        return spec;
    }
}
