package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalCreatedEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalReturnEventDto;
import com.carshare.rentalsystem.exception.AccessDeniedException;
import com.carshare.rentalsystem.exception.CarNotAvailableException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.RentalAlreadyReturnedException;
import com.carshare.rentalsystem.mapper.RentalMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.car.CarRepository;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import com.carshare.rentalsystem.repository.rental.RentalSpecificationBuilder;
import com.carshare.rentalsystem.repository.user.UserRepository;
import java.time.LocalDate;
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
    private static final int MIN_INVENTORY_TO_RENT = 1;
    private static final int CAR_INVENTORY_DECREMENT = 1;
    private static final int CAR_INVENTORY_INCREMENT = 1;

    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto) {
        Car car = findCarById(requestDto.getCarId());

        if (car.getInventory() < MIN_INVENTORY_TO_RENT) {
            throw new CarNotAvailableException("Car with id: "
                    + requestDto.getCarId() + " is out of stock");
        }

        car.setInventory(car.getInventory() - CAR_INVENTORY_DECREMENT);
        carRepository.save(car);

        Rental rental = rentalMapper.toEntity(requestDto);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with id: " + userId)
        );

        rental.setUser(user);
        rental.setCar(car);
        RentalResponseDto rentalResponseDto = rentalMapper.toDto(rentalRepository.save(rental));

        applicationEventPublisher.publishEvent(
                new RentalCreatedEventDto(rental, rental.getUser().getId())
        );

        return rentalResponseDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalResponseDto> getRentalsById(Long userId, Pageable pageable) {
        return rentalRepository.findAllByUserIdWithCarAndUser(userId, pageable)
                .map(rentalMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalResponseDto> getSpecificRentals(
            RentalSearchParameters searchParameters, Pageable pageable) {
        Specification<Rental> rentalSpecification =
                rentalSpecificationBuilder.build(searchParameters);

        return rentalRepository.findAll(rentalSpecification, pageable)
                .map(rentalMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalPreviewResponseDto> getAllRentalsPreview(boolean isManager,
                                                               Long userId, Pageable pageable) {
        Page<Rental> rentals = isManager
                ? rentalRepository.findAllWithUser(pageable)
                : rentalRepository.findAllByUserIdWithUser(userId, pageable);

        return rentals.map(rentalMapper::toPreviewDto);
    }

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

        if (rental.getActualReturnDate() != null) {
            throw new RentalAlreadyReturnedException("This rental has already been returned");
        }

        if (!rental.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to return this rental");
        }

        rental.setActualReturnDate(LocalDate.now());
        rentalRepository.save(rental);

        Car car = findCarById(rental.getCar().getId());
        car.setInventory(car.getInventory() + CAR_INVENTORY_INCREMENT);
        carRepository.save(car);

        applicationEventPublisher.publishEvent(
                new RentalReturnEventDto(rental, rental.getUser().getId())
        );

        return rentalMapper.toDto(rental);
    }

    private Car findCarById(Long carId) {
        return carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: " + carId)
        );
    }

    private Rental findRentalById(Long rentalId) {
        return rentalRepository.findByIdWithCarAndUser(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );
    }
}
