package com.carshare.rentalsystem.repository.rental;

import com.carshare.rentalsystem.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RentalRepository extends JpaRepository<Rental, Long>,
        JpaSpecificationExecutor<Rental>, PagingAndSortingRepository<Rental, Long> {
    Page<Rental> findAllByUserId(Long userId, Pageable pageable);
}
