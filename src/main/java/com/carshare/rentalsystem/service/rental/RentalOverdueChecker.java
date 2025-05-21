package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalDueSoonEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalOverdueEventDto;
import com.carshare.rentalsystem.mapper.RentalMapper;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalOverdueChecker {
    private static final int DUE_SOON_THRESHOLD_DAYS = 3;
    private static final int DUE_SOON_MIN_DAYS = 0;

    private final RentalRepository rentalRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RentalMapper rentalMapper;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void checkRentalOverdue() {
        LocalDate today = LocalDate.now();

        List<Rental> rentals = rentalRepository.findAll();

        for (Rental rental : rentals) {
            LocalDate returnDate = rental.getReturnDate();
            LocalDate actualReturnDate = rental.getActualReturnDate();

            if (actualReturnDate != null) {
                continue;
            }

            long daysUntilReturn = ChronoUnit.DAYS.between(today, returnDate);

            if (daysUntilReturn <= DUE_SOON_THRESHOLD_DAYS
                    && daysUntilReturn >= DUE_SOON_MIN_DAYS) {
                applicationEventPublisher.publishEvent(new RentalDueSoonEventDto(
                        rentalMapper.toDto(rental),
                        rental.getUser().getId()
                ));
            } else if (returnDate.isBefore(today)) {
                applicationEventPublisher.publishEvent(new RentalOverdueEventDto(
                        rentalMapper.toDto(rental),
                        rental.getUser().getId()
                ));
            }
        }
    }
}
