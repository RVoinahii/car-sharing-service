package com.carshare.rentalsystem.repository.rental;

import com.carshare.rentalsystem.model.Rental;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long>,
        JpaSpecificationExecutor<Rental>, PagingAndSortingRepository<Rental, Long> {

    @Query("SELECT r FROM Rental r JOIN FETCH r.car c JOIN FETCH r.user u"
            + " WHERE r.user.id = :userId")
    Page<Rental> findAllByUserIdWithCarAndUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Rental r JOIN FETCH r.user"
            + " WHERE r.user.id = :userId")
    Page<Rental> findAllByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Rental r JOIN FETCH r.user")
    Page<Rental> findAllWithUser(Pageable pageable);

    @Query("SELECT r FROM Rental r JOIN FETCH r.car c JOIN FETCH r.user u WHERE r.id = :rentalId")
    Optional<Rental> findByIdWithCarAndUser(@Param("rentalId") Long rentalId);

    @Query("SELECT r FROM Rental r JOIN FETCH r.car c JOIN FETCH r.user u "
            + "WHERE r.id = :rentalId AND r.user.id = :userId")
    Optional<Rental> findByIdAndUserIdWithCarAndUser(
            @Param("rentalId") Long rentalId, @Param("userId") Long userId);
}
