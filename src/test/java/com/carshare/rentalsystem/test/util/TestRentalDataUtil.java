package com.carshare.rentalsystem.test.util;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.DEFAULT_ID_SAMPLE;

import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import java.time.LocalDate;

public class TestRentalDataUtil {
    public static final LocalDate RENTAL_DATE = LocalDate.now();
    public static final LocalDate RENTAL_RETURN_DATE = LocalDate.now().plusDays(1);
    public static final LocalDate RENTAL_ACTUAL_RETURN_DATE = LocalDate.now().plusDays(1);

    public static CreateRentalRequestDto createRentalRequestDtoSample() {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();
        requestDto.setCarId(DEFAULT_ID_SAMPLE);
        requestDto.setRentalDate(RENTAL_DATE);

        requestDto.setReturnDate(RENTAL_RETURN_DATE);
        return requestDto;
    }

    public static Rental createRentalFromRequest(CreateRentalRequestDto requestDto) {
        Rental rental = new Rental();
        rental.setId(DEFAULT_ID_SAMPLE);
        rental.setRentalDate(requestDto.getRentalDate());
        rental.setReturnDate(requestDto.getReturnDate());
        rental.setActualReturnDate(null);

        Car car = new Car();
        car.setId(requestDto.getCarId());
        rental.setCar(car);

        User user = new User();
        user.setId(DEFAULT_ID_SAMPLE);
        rental.setUser(user);

        if (requestDto.getRentalDate().isAfter(LocalDate.now())) {
            rental.setStatus(Rental.RentalStatus.RESERVED);
        } else {
            rental.setStatus(Rental.RentalStatus.ACTIVE);
        }

        return rental;
    }

    public static Rental createCompletedRentalSample() {
        Rental rental = new Rental();
        rental.setId(DEFAULT_ID_SAMPLE);
        rental.setRentalDate(RENTAL_DATE);
        rental.setReturnDate(RENTAL_RETURN_DATE);
        rental.setActualReturnDate(RENTAL_ACTUAL_RETURN_DATE);

        Car car = new Car();
        car.setId(DEFAULT_ID_SAMPLE);
        rental.setCar(car);

        User user = new User();
        user.setId(DEFAULT_ID_SAMPLE);
        rental.setUser(user);

        rental.setStatus(Rental.RentalStatus.COMPLETED);

        return rental;
    }

    public static Rental createActiveRentalSample() {
        Rental rental = new Rental();
        rental.setId(DEFAULT_ID_SAMPLE);
        rental.setRentalDate(RENTAL_DATE);
        rental.setReturnDate(RENTAL_RETURN_DATE);
        rental.setActualReturnDate(null);

        Car car = new Car();
        car.setId(DEFAULT_ID_SAMPLE);
        rental.setCar(car);

        User user = new User();
        user.setId(DEFAULT_ID_SAMPLE);
        rental.setUser(user);

        rental.setStatus(Rental.RentalStatus.ACTIVE);

        return rental;
    }

    public static Rental createReservedRentalSample() {
        Rental rental = new Rental();
        rental.setId(DEFAULT_ID_SAMPLE);
        rental.setRentalDate(RENTAL_DATE.plusDays(5));
        rental.setReturnDate(RENTAL_RETURN_DATE.plusDays(5));
        rental.setActualReturnDate(null);

        Car car = new Car();
        car.setId(DEFAULT_ID_SAMPLE);
        rental.setCar(car);

        User user = new User();
        user.setId(DEFAULT_ID_SAMPLE);
        rental.setUser(user);

        rental.setStatus(Rental.RentalStatus.RESERVED);

        return rental;
    }

    public static RentalResponseDto createCompletedRentalDtoSample() {
        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setRentalDate(RENTAL_DATE);
        responseDto.setReturnDate(RENTAL_RETURN_DATE);
        responseDto.setActualReturnDate(RENTAL_ACTUAL_RETURN_DATE);

        CarPreviewResponseDto carDto = new CarPreviewResponseDto();
        carDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setCar(carDto);

        UserPreviewResponseDto userDto = new UserPreviewResponseDto();
        userDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setUser(userDto);

        responseDto.setStatus(Rental.RentalStatus.COMPLETED.name());

        return responseDto;
    }

    public static RentalResponseDto createActiveRentalDtoSample() {
        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setRentalDate(RENTAL_DATE);
        responseDto.setReturnDate(RENTAL_RETURN_DATE);
        responseDto.setActualReturnDate(null);

        CarPreviewResponseDto carDto = new CarPreviewResponseDto();
        carDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setCar(carDto);

        UserPreviewResponseDto userDto = new UserPreviewResponseDto();
        userDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setUser(userDto);

        responseDto.setStatus(Rental.RentalStatus.ACTIVE.name());

        return responseDto;
    }

    public static RentalResponseDto createRentalDtoSampleFromEntity(Rental rental) {
        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(rental.getId());
        responseDto.setRentalDate(rental.getRentalDate());
        responseDto.setReturnDate(rental.getReturnDate());
        responseDto.setActualReturnDate(rental.getActualReturnDate());

        CarPreviewResponseDto carDto = new CarPreviewResponseDto();
        carDto.setId(rental.getCar().getId());
        responseDto.setCar(carDto);

        UserPreviewResponseDto userDto = new UserPreviewResponseDto();
        userDto.setId(rental.getUser().getId());
        responseDto.setUser(userDto);

        responseDto.setStatus(rental.getStatus().name());

        return responseDto;
    }

    public static RentalPreviewResponseDto createCompletedRentalPreviewDtoSample() {
        RentalPreviewResponseDto responseDto = new RentalPreviewResponseDto();
        responseDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setCarId(DEFAULT_ID_SAMPLE);
        responseDto.setUserId(DEFAULT_ID_SAMPLE);
        responseDto.setStatus(Rental.RentalStatus.COMPLETED.name());

        return responseDto;
    }

    public static RentalPreviewResponseDto createRentalPreviewDtoSampleFromEntity(Rental rental) {
        RentalPreviewResponseDto responseDto = new RentalPreviewResponseDto();
        responseDto.setId(rental.getId());
        responseDto.setCarId(rental.getCar().getId());
        responseDto.setUserId(rental.getUser().getId());
        responseDto.setStatus(rental.getStatus().name());

        return responseDto;
    }
}
