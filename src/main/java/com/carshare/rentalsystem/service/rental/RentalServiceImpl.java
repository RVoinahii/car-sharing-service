package com.carshare.rentalsystem.service.rental;

import com.carshare.rentalsystem.dto.rental.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.RentalDetailsResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalSearchParameters;
import com.carshare.rentalsystem.exception.AccessDeniedException;
import com.carshare.rentalsystem.exception.CarNotAvailableException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;

    @Override
    public RentalResponseDto create(Long userId, CreateRentalRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: "
                        + requestDto.getCarId())
        );

        if (car.getInventory() < 1) {
            throw new CarNotAvailableException("Car with id: "
                    + requestDto.getCarId() + " is out of stock");
        }

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        Rental rental = rentalMapper.toEntity(requestDto);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with id: " + userId)
        );

        rental.setUser(user);
        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public Page<RentalDetailsResponseDto> getRentalsById(Long userId, Pageable pageable) {
        return rentalRepository.findAllByUserId(userId, pageable)
                .map(rentalMapper::toDetailsDto);
    }

    @Override
    public Page<RentalDetailsResponseDto> getSpecificRentals(
            RentalSearchParameters searchParameters, Pageable pageable) {
        Specification<Rental> rentalSpecification =
                rentalSpecificationBuilder.build(searchParameters);
        return rentalRepository.findAll(rentalSpecification, pageable)
                .map(rentalMapper::toDetailsDto);
    }

    @Override
    public RentalDetailsResponseDto getRentalInfo(Long userId, Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );

        if (!rental.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Can't find rental with id: " + rentalId);
        }
        return rentalMapper.toDetailsDto(rental);
    }

    @Override
    public RentalDetailsResponseDto returnRental(Long userId, Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );

        if (rental.getActualReturnDate() != null) {
            throw new IllegalStateException("This rental has already been returned");
        }

        if (!rental.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are now allowed to return this rental");
        }

        rental.setActualReturnDate(LocalDate.now());
        rentalRepository.save(rental);
        Car car = carRepository.findById(rental.getCar().getId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: "
                        + rental.getCar().getId())
        );
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        return rentalMapper.toDetailsDto(rental);
    }
}
