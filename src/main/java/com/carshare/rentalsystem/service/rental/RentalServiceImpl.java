package com.carshare.rentalsystem.service.rental;

import static com.carshare.rentalsystem.service.payment.RentalPaymentCalculator.DAYS_INCLUSIVE_OFFSET;
import static java.time.temporal.ChronoUnit.DAYS;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalCreatedEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalReturnEventDto;
import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.exception.CarNotAvailableException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.MaxActiveRentalsExceededException;
import com.carshare.rentalsystem.exception.RentalAccessDeniedException;
import com.carshare.rentalsystem.exception.RentalAlreadyReturnedException;
import com.carshare.rentalsystem.exception.TooLateToCancelRentalException;
import com.carshare.rentalsystem.mapper.RentalMapper;
import com.carshare.rentalsystem.mapper.search.RentalSearchParametersMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.SpecificationBuilderFactory;
import com.carshare.rentalsystem.repository.car.CarRepository;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import com.carshare.rentalsystem.repository.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private static final boolean DECREASE_CAR_INVENTORY = false;
    private static final boolean INCREASE_CAR_INVENTORY = true;
    private static final int MAX_ACTIVE_RENTALS = 3;
    private static final int CAR_INVENTORY_STEP = 1;
    private static final int MIN_CAR_INVENTORY_TO_RENT = 1;
    private static final int FREE_CANCELLATION_THRESHOLD = 4;
    private static final int MAX_EARLY_CANCEL_DAYS = 3;

    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final RentalSearchParametersMapper searchParametersMapper;
    private final SpecificationBuilderFactory specificationBuilderFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto) {
        long activeRentals = rentalRepository.countUserActiveRentals(
                userId,
                List.of(Rental.RentalStatus.RESERVED, Rental.RentalStatus.WAITING_FOR_PAYMENT,
                        Rental.RentalStatus.ACTIVE));

        if (activeRentals >= MAX_ACTIVE_RENTALS) {
            throw new MaxActiveRentalsExceededException("User already has " + activeRentals
                    + " active rentals. Maximum allowed: " + MAX_ACTIVE_RENTALS);
        }

        Car car = updateCarInventory(requestDto.getCarId(), DECREASE_CAR_INVENTORY);

        Rental rental = rentalMapper.toEntity(requestDto);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with id: " + userId)
        );

        rental.setUser(user);
        rental.setCar(car);
        rental.setStatus(resolveInitialStatus(rental));

        RentalResponseDto responseDto = rentalMapper.toDto(rentalRepository.save(rental));

        applicationEventPublisher.publishEvent(
                new RentalCreatedEventDto(responseDto, rental.getUser().getId())
        );

        return responseDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalPreviewResponseDto> getRentalsById(Long userId, Pageable pageable) {
        return rentalRepository.findAllByUserIdWithCarAndUser(userId, pageable)
                .map(rentalMapper::toPreviewDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalPreviewResponseDto> getSpecificRentals(
            RentalSearchParameters searchParameters, Pageable pageable) {
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<Rental> rentalSpecification = specificationBuilderFactory
                .getBuilder(Rental.class)
                .build(filters);

        return rentalRepository.findAll(rentalSpecification, pageable)
                .map(rentalMapper::toPreviewDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RentalResponseDto getAnyRentalInfo(Long rentalId) {
        Rental rental = findRentalById(rentalId);

        return rentalMapper.toDto(rental);
    }

    @Transactional(readOnly = true)
    @Override
    public RentalResponseDto getCustomerRentalInfo(Long userId, Long rentalId) {
        Rental rental = findRentalById(rentalId);

        if (!rental.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Can't find rental with id: " + rentalId);
        }

        return rentalMapper.toDto(rental);
    }

    @Transactional
    @Override
    public RentalResponseDto returnRental(Long userId, Long rentalId) {
        Rental rental = findRentalById(rentalId);

        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalAccessDeniedException(
                    "You do not have permission to return this rental");
        }

        if (rental.getActualReturnDate() != null) {
            throw new RentalAlreadyReturnedException("This rental has already been returned");
        }

        LocalDate today = LocalDate.now();
        LocalDate rentalStartDate = rental.getRentalDate();

        if (rental.getStatus() == Rental.RentalStatus.RESERVED
                && !today.isBefore(rentalStartDate.minusDays(MAX_EARLY_CANCEL_DAYS))) {
            throw new TooLateToCancelRentalException(
                    "You can only cancel a reserved rental at least "
                            + MAX_EARLY_CANCEL_DAYS + " days in advance");
        }

        rental.setActualReturnDate(today);
        rental.setStatus(resolveReturnStatus(rental));
        rentalRepository.save(rental);

        updateCarInventory(rental.getCar().getId(), INCREASE_CAR_INVENTORY);

        RentalResponseDto responseDto = rentalMapper.toDto(rental);

        applicationEventPublisher.publishEvent(
                new RentalReturnEventDto(responseDto, rental.getUser().getId())
        );

        return responseDto;
    }

    private Car updateCarInventory(Long carId, boolean increase) {
        Car car = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: " + carId)
        );

        int currentInventory = car.getInventory();
        int newInventory = increase
                ? currentInventory + CAR_INVENTORY_STEP
                : currentInventory - CAR_INVENTORY_STEP;

        if (!increase && newInventory < MIN_CAR_INVENTORY_TO_RENT) {
            throw new CarNotAvailableException("Car with id: " + carId + " is out of stock");
        }

        car.setInventory(newInventory);
        return carRepository.save(car);
    }

    private Rental.RentalStatus resolveInitialStatus(Rental rental) {
        LocalDate today = LocalDate.now();

        if (rental.getRentalDate().isAfter(today)) {
            return Rental.RentalStatus.RESERVED;
        } else {
            return Rental.RentalStatus.ACTIVE;
        }
    }

    private Rental.RentalStatus resolveReturnStatus(Rental rental) {
        if (rental.getStatus() != Rental.RentalStatus.RESERVED) {
            return Rental.RentalStatus.WAITING_FOR_PAYMENT;
        }

        long rentalLength = DAYS.between(rental.getRentalDate(), rental.getReturnDate())
                + DAYS_INCLUSIVE_OFFSET;

        return rentalLength <= FREE_CANCELLATION_THRESHOLD
                ? Rental.RentalStatus.CANCELLED
                : Rental.RentalStatus.WAITING_FOR_PAYMENT;
    }

    private Rental findRentalById(Long rentalId) {
        return rentalRepository.findByIdWithCarAndUser(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );
    }
}
