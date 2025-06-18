package com.carshare.rentalsystem.repository;

public interface SpecificationProviderRegistry {
    <T> SpecificationProvider<T> getSpecificationProvider(Class<T> entityType, String key);
}
