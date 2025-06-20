package com.carshare.rentalsystem.repository.rental;

import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_NUMBER;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carshare.rentalsystem.model.Rental;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RentalRepositoryTests {
    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("""
            findAllByUserIdWithCarAndUser():
             Should return rentals with car and user for provided userId
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByUserIdWithCarAndUser_ExistingUserId_ReturnsRentals() {
        //Given
        Long userId = 3L;
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        Page<Rental> rentals = rentalRepository.findAllByUserIdWithCarAndUser(userId, pageable);

        //Then
        assertFalse(rentals.isEmpty(), "Should return rentals for user");
        rentals.forEach(rental -> {
            assertEquals(userId, rental.getUser().getId(), "Rental should belong to user");
            assertNotNull(rental.getCar(), "Car should be fetched");
        });
    }

    @Test
    @DisplayName("""
        findAllByUserIdWithCarAndUser():
         Should return empty page for non-existent userId
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByUserIdWithCarAndUser_invalidUserId_ReturnsEmptyPage() {
        //Given
        Long invalidUserId = 99L;
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        Page<Rental> rentals = rentalRepository.findAllByUserIdWithCarAndUser(
                invalidUserId, pageable);

        //Then
        assertTrue(rentals.isEmpty(), "Should return empty page for non-existent userId");
    }

    @Test
    @DisplayName("""
            findAll(Specification):
             Should return rentals matching specification
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_WithSpecification_ReturnsRentals() {
        //Given
        Specification<Rental> spec = (root, query, cb) ->
                cb.equal(root.get("status"), Rental.RentalStatus.ACTIVE);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        Page<Rental> rentals = rentalRepository.findAll(spec, pageable);

        //Then
        assertFalse(rentals.isEmpty(), "Should return ACTIVE rentals");
        rentals.forEach(rental ->
                assertEquals(Rental.RentalStatus.ACTIVE, rental.getStatus(),
                        "Rental status should be ACTIVE"));
    }

    @Test
    @DisplayName("""
        findAll(Specification):
         Should return empty page when no rentals match specification
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_WithSpecification_NoMatch_ReturnsEmptyPage() {
        //Given
        Specification<Rental> spec = (root, query, cb) ->
                cb.equal(root.get("status"), Rental.RentalStatus.CANCELLED);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        Page<Rental> rentals = rentalRepository.findAll(spec, pageable);

        //Then
        assertTrue(rentals.isEmpty(), "Should return empty page when no rentals match");
    }

    @Test
    @DisplayName("""
            findByIdWithCarAndUser():
             Should return rental with car and user when rental exists
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByIdWithCarAndUser_ExistedRental_ReturnsRentalWithCarAndUser() {
        //Given
        Long rentalId = 1L;

        //When
        Optional<Rental> actualRental = rentalRepository.findByIdWithCarAndUser(rentalId);

        //Then
        assertTrue(actualRental.isPresent(), "Rental should be present");
        assertNotNull(actualRental.get().getCar(), "Car should be fetched");
        assertNotNull(actualRental.get().getUser(), "User should be fetched");
    }

    @Test
    @DisplayName("""
            findByIdAndUserIdWithCarAndUser():
             Should return rental with car and user for given rentalId and userId
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByIdAndUserIdWithCarAndUser_ExistingIds_ReturnsRental() {
        //Given
        Long rentalId = 1L;
        Long userId = 3L;

        //When
        Optional<Rental> actualRental = rentalRepository.findByIdAndUserIdWithCarAndUser(
                rentalId, userId);

        //Then
        assertTrue(actualRental.isPresent(), "Rental should be present");
        assertEquals(userId, actualRental.get().getUser().getId(),
                "Rental should belong to user");
        assertNotNull(actualRental.get().getCar(), "Car should be fetched");
    }

    @Test
    @DisplayName("""
        findByIdWithCarAndUser():
         Should return empty for non-existent rentalId
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByIdWithCarAndUser_invalidRentalId_ReturnsEmpty() {
        //Given
        Long invalidRentalId = 999L;

        //When
        Optional<Rental> rental = rentalRepository.findByIdWithCarAndUser(invalidRentalId);

        //Then
        assertTrue(rental.isEmpty(), "Should return empty optional for non-existent rentalId");
    }

    @Test
    @DisplayName("""
        findByIdAndUserIdWithCarAndUser():
         Should return empty for non-existent rentalId or userId mismatch
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByIdAndUserIdWithCarAndUser_InvalidOrMismatchedIds_ReturnsEmpty() {
        //Given
        Long validRentalId = 1L;
        Long invalidUserId = 999L;
        Long invalidRentalId = 999L;
        Long validUserId = 3L;

        //When
        Optional<Rental> rentalByWrongUser = rentalRepository.findByIdAndUserIdWithCarAndUser(
                validRentalId, invalidUserId);
        Optional<Rental> rentalNonExistent = rentalRepository.findByIdAndUserIdWithCarAndUser(
                invalidRentalId, validUserId);

        //Then
        assertTrue(rentalByWrongUser.isEmpty(),
                "Should return empty optional when userId mismatched");
        assertTrue(rentalNonExistent.isEmpty(),
                "Should return empty optional for non-existent rentalId");
    }

    @Test
    @DisplayName("""
            findActiveRentalsDueOrOverdue():
             Should return active rentals due or overdue
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_two_active_due_overdue_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findActiveRentalsDueOrOverdue_ShouldReturnRentals() {
        //Given
        Rental.RentalStatus status = Rental.RentalStatus.ACTIVE;
        LocalDate maxDate = LocalDate.now();
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        Page<Rental> rentals = rentalRepository.findActiveRentalsDueOrOverdue(
                status, maxDate, pageable);

        //Then
        assertFalse(rentals.isEmpty(), "Should return active due/overdue rentals");
        rentals.forEach(rental -> {
            assertEquals(status, rental.getStatus(), "Rental status should be ACTIVE");
            assertTrue(rental.getReturnDate().isBefore(maxDate.plusDays(1)),
                    "Return date should be due/overdue");
            assertNull(rental.getActualReturnDate(), "Actual return date should be null");
        });
    }

    @Test
    @DisplayName("""
            findReservedReadyToActivate():
             Should return reserved rentals ready to activate
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_two_reserved_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findReservedReadyToActivate_ShouldReturnRentals() {
        //Given
        Rental.RentalStatus status = Rental.RentalStatus.RESERVED;
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        Page<Rental> rentals = rentalRepository.findReservedReadyToActivate(
                status, today, pageable);

        //Then
        assertFalse(rentals.isEmpty(), "Should return rentals ready to activate");
        rentals.forEach(rental ->
                assertEquals(status, rental.getStatus(), "Rental status should be RESERVED"));
    }

    @Test
    @DisplayName("""
            countUserActiveRentals():
             Should return correct count of user active rentals
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_and_one_reserved_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void countUserActiveRentals_ShouldReturnCorrectCount() {
        //Given
        Long userId = 3L;
        List<Rental.RentalStatus> activeStatuses = List.of(
                Rental.RentalStatus.ACTIVE,
                Rental.RentalStatus.RESERVED
        );

        //When
        long count = rentalRepository.countUserActiveRentals(userId, activeStatuses);

        //Then
        assertEquals(2L, count, "Active rental count should match expected");
    }

    @Test
    @DisplayName("""
        countUserActiveRentals():
         Should return 0 when no active rentals exist for user
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_completed_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void countUserActiveRentals_NoActiveRentals_ReturnsZero() {
        //Given
        Long userId = 3L;
        List<Rental.RentalStatus> activeStatuses = List.of(
                Rental.RentalStatus.ACTIVE,
                Rental.RentalStatus.RESERVED
        );

        //When
        long count = rentalRepository.countUserActiveRentals(userId, activeStatuses);

        //Then
        assertEquals(0L, count, "Should return 0 when user has no active rentals");
    }
}
