package com.carshare.rentalsystem.repository;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T, V> {
    Specification<T> build(V params);
}
