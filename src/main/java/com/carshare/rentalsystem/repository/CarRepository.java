package com.carshare.rentalsystem.repository;

import com.carshare.rentalsystem.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
