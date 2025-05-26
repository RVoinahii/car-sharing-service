package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalDueSoonEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalOverdueEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalStartEventDto;
import com.carshare.rentalsystem.mapper.RentalMapper;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalStatusChecker {
    public static final int DEFAULT_PAGE_INDEX = 0;
    public static final int DEFAULT_PAGE_SIZE = 100;

    private static final int DUE_SOON_THRESHOLD_DAYS = 3;
    private static final int DUE_SOON_MIN_DAYS = 0;

    private final RentalRepository rentalRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RentalMapper rentalMapper;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkRentalOverdue() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE_INDEX, DEFAULT_PAGE_SIZE);
        LocalDate today = LocalDate.now();

        processReservedRentals(today, pageable);
        processActiveRentals(today, pageable);
    }

    @Transactional
    private void processReservedRentals(LocalDate today, Pageable pageable) {
        Page<Rental> page;
        List<Rental> updatedRentals = new ArrayList<>();
        do {
            page = rentalRepository.findReservedReadyToActivate(
                    Rental.RentalStatus.RESERVED, today, pageable);

            for (Rental rental : page.getContent()) {
                rental.setStatus(Rental.RentalStatus.ACTIVE);
                updatedRentals.add(rental);
                applicationEventPublisher.publishEvent(new RentalStartEventDto(
                        rentalMapper.toDto(rental),
                        rental.getUser().getId()
                ));
            }

            if (!updatedRentals.isEmpty()) {
                rentalRepository.saveAll(updatedRentals);
                updatedRentals.clear();
            }
            pageable = pageable.next();
        } while (page.hasNext());
    }

    @Transactional(readOnly = true)
    private void processActiveRentals(LocalDate today, Pageable pageable) {
        LocalDate maxDate = today.plusDays(DUE_SOON_THRESHOLD_DAYS);
        Page<Rental> activeRentals = rentalRepository.findActiveRentalsDueOrOverdue(
                Rental.RentalStatus.ACTIVE, maxDate, pageable);

        for (Rental rental : activeRentals) {
            handleDueSoon(rental, today);
            handleOverdue(rental, today);
        }
    }

    private void handleDueSoon(Rental rental, LocalDate today) {
        LocalDate returnDate = rental.getReturnDate();
        long daysUntilReturn = ChronoUnit.DAYS.between(today, returnDate);

        if (daysUntilReturn <= DUE_SOON_THRESHOLD_DAYS && daysUntilReturn >= DUE_SOON_MIN_DAYS) {
            applicationEventPublisher.publishEvent(new RentalDueSoonEventDto(
                    rentalMapper.toDto(rental),
                    rental.getUser().getId()
            ));
        }
    }

    private void handleOverdue(Rental rental, LocalDate today) {
        LocalDate returnDate = rental.getReturnDate();
        if (returnDate.isBefore(today) && rental.getActualReturnDate() == null) {
            applicationEventPublisher.publishEvent(new RentalOverdueEventDto(
                    rentalMapper.toDto(rental),
                    rental.getUser().getId()
            ));
        }
    }
}
