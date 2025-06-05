package com.carshare.rentalsystem.repository.review.rental;

import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.RentalReview;
import com.carshare.rentalsystem.model.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RentalReviewRepository extends JpaRepository<RentalReview, Long>,
        JpaSpecificationExecutor<RentalReview>, PagingAndSortingRepository<RentalReview, Long> {
    Optional<RentalReview> findByRentalAndUser(Rental rental, User user);

    @EntityGraph(attributePaths = {"rental", "rental.user", "rental.car"})
    Page<RentalReview> findAll(Specification<RentalReview> spec, Pageable pageable);
}
