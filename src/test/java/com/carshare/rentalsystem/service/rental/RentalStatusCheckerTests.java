package com.carshare.rentalsystem.service.rental;

import static com.carshare.rentalsystem.service.rental.RentalStatusChecker.DEFAULT_PAGE_SIZE;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createActiveRentalSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createRentalDtoSampleFromEntity;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createReservedRentalSample;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalDueSoonEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalOverdueEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalStartEventDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.mapper.RentalMapper;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class RentalStatusCheckerTests {
    private static final LocalDate TODAY = LocalDate.now();

    @InjectMocks
    private RentalStatusChecker rentalStatusChecker;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    @DisplayName("""
    checkRentalOverdue():
     Should update RESERVED rentals to ACTIVE if rentalDate is today or in past
            """)
    void checkRentalOverdue_ShouldUpdateReservedRentalsToActive() {
        // Given
        Pageable firstPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        Rental reservedRental = createReservedRentalSample();
        reservedRental.setRentalDate(TODAY.minusDays(1));
        reservedRental.setReturnDate(TODAY.plusDays(5));

        RentalResponseDto rentalResponseDto = createRentalDtoSampleFromEntity(reservedRental);

        Page<Rental> firstPage = new PageImpl<>(
                List.of(reservedRental),
                firstPageable,
                1
        );

        when(rentalRepository.findReservedReadyToActivate(
                eq(Rental.RentalStatus.RESERVED),
                eq(TODAY),
                argThat(pageable -> pageable.getPageNumber() == 0)))
                .thenReturn(firstPage);

        when(rentalRepository.findActiveRentalsDueOrOverdue(
                any(),
                any(),
                any()))
                .thenReturn(Page.empty());

        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalResponseDto);

        //When
        rentalStatusChecker.checkRentalOverdue();

        //Then
        verify(rentalRepository).saveAll(anyList());

        verify(applicationEventPublisher).publishEvent(any(RentalStartEventDto.class));

        verify(rentalRepository).findReservedReadyToActivate(
                eq(Rental.RentalStatus.RESERVED),
                eq(TODAY),
                any(Pageable.class));

        verify(rentalRepository).findActiveRentalsDueOrOverdue(
                eq(Rental.RentalStatus.ACTIVE),
                any(LocalDate.class),
                any(Pageable.class));

        verifyNoMoreInteractions(rentalRepository, applicationEventPublisher, rentalMapper);
    }

    @Test
    @DisplayName("""
    checkRentalOverdue():
    Should publish RentalDueSoonEventDto for rentals due soon (0-3 days)
            """)
    void checkRentalOverdue_ShouldPublishDueSoonEvent() {
        // Given
        Pageable firstPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        Rental activeRentalDueSoon = createActiveRentalSample();
        activeRentalDueSoon.setReturnDate(TODAY.plusDays(2));

        RentalResponseDto rentalResponseDto = createRentalDtoSampleFromEntity(activeRentalDueSoon);

        Page<Rental> firstPage = new PageImpl<>(
                List.of(activeRentalDueSoon),
                firstPageable,
                1
        );

        when(rentalRepository.findReservedReadyToActivate(
                any(),
                any(),
                any()))
                .thenReturn(Page.empty());

        when(rentalRepository.findActiveRentalsDueOrOverdue(
                eq(Rental.RentalStatus.ACTIVE),
                any(LocalDate.class),
                argThat(pageable -> pageable.getPageNumber() == 0)))
                .thenReturn(firstPage);

        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalResponseDto);

        // When
        rentalStatusChecker.checkRentalOverdue();

        // Then
        verify(applicationEventPublisher).publishEvent(any(RentalDueSoonEventDto.class));

        verify(rentalRepository).findReservedReadyToActivate(
                eq(Rental.RentalStatus.RESERVED),
                eq(TODAY),
                any(Pageable.class));

        verify(rentalRepository).findActiveRentalsDueOrOverdue(
                eq(Rental.RentalStatus.ACTIVE),
                any(LocalDate.class),
                any(Pageable.class));

        verifyNoMoreInteractions(rentalRepository, applicationEventPublisher, rentalMapper);
    }

    @Test
    @DisplayName("""
    checkRentalOverdue():
    Should publish RentalOverdueEventDto for overdue rentals (returnDate before today, not returned)
            """)
    void checkRentalOverdue_ShouldPublishOverdueEvent() {
        // Given
        Pageable firstPageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        Rental activeRentalOverdue = createActiveRentalSample();
        activeRentalOverdue.setReturnDate(TODAY.minusDays(1));

        RentalResponseDto rentalResponseDto = createRentalDtoSampleFromEntity(activeRentalOverdue);

        Page<Rental> firstPage = new PageImpl<>(
                List.of(activeRentalOverdue),
                firstPageable,
                1
        );

        when(rentalRepository.findReservedReadyToActivate(
                any(),
                any(),
                any()))
                .thenReturn(Page.empty());

        when(rentalRepository.findActiveRentalsDueOrOverdue(
                eq(Rental.RentalStatus.ACTIVE),
                any(LocalDate.class),
                argThat(pageable -> pageable.getPageNumber() == 0)))
                .thenReturn(firstPage);

        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalResponseDto);

        // When
        rentalStatusChecker.checkRentalOverdue();

        // Then
        verify(applicationEventPublisher).publishEvent(any(RentalOverdueEventDto.class));

        verify(rentalRepository).findReservedReadyToActivate(
                eq(Rental.RentalStatus.RESERVED),
                eq(TODAY),
                any(Pageable.class));

        verify(rentalRepository).findActiveRentalsDueOrOverdue(
                eq(Rental.RentalStatus.ACTIVE),
                any(LocalDate.class),
                any(Pageable.class));

        verifyNoMoreInteractions(rentalRepository, applicationEventPublisher, rentalMapper);
    }
}
