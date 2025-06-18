package com.carshare.rentalsystem.repository.car;

import com.carshare.rentalsystem.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CarRepository extends JpaRepository<Car, Long>,
        JpaSpecificationExecutor<Car>, PagingAndSortingRepository<Car, Long> {
}
