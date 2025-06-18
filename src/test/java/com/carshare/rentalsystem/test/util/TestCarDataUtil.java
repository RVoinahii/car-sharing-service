package com.carshare.rentalsystem.test.util;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.DEFAULT_ID_SAMPLE;

import com.carshare.rentalsystem.dto.car.request.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarResponseDto;
import com.carshare.rentalsystem.model.Car;
import java.math.BigDecimal;

public class TestCarDataUtil {
    public static final int PAGE_NUMBER = 0;
    public static final int PAGE_SIZE = 10;
    public static final String CAR_MODEL = "CarModel";
    public static final String CAR_NEW_MODEL = "CarNewModel";
    public static final String CAR_NEW_BRAND = "CarNewBrand";
    public static final String CAR_BRAND = "CarBrand";
    public static final Car.Type CAR_TYPE = Car.Type.SEDAN;
    public static final int CAR_INVENTORY = 15;
    public static final BigDecimal CAR_DAILY_FEE = BigDecimal.valueOf(39.99);

    public static CreateCarRequestDto createCarRequestDtoSample() {
        return new CreateCarRequestDto(
                CAR_MODEL,
                CAR_BRAND,
                CAR_TYPE,
                CAR_INVENTORY,
                CAR_DAILY_FEE
        );
    }

    public static CreateCarRequestDto createUpdateCarRequestDtoSample() {
        return new CreateCarRequestDto(
                CAR_NEW_MODEL,
                CAR_NEW_BRAND,
                CAR_TYPE,
                CAR_INVENTORY,
                CAR_DAILY_FEE
        );
    }

    public static Car createDefaultCarSample() {
        Car car = new Car();
        car.setId(DEFAULT_ID_SAMPLE);
        car.setModel(CAR_MODEL);
        car.setBrand(CAR_BRAND);
        car.setType(CAR_TYPE);
        car.setInventory(CAR_INVENTORY);
        car.setDailyFee(CAR_DAILY_FEE);

        return car;
    }

    public static Car createCarSampleFromRequest(CreateCarRequestDto requestDto) {
        Car car = new Car();
        car.setId(DEFAULT_ID_SAMPLE);
        car.setModel(requestDto.model());
        car.setBrand(requestDto.brand());
        car.setType(requestDto.type());
        car.setInventory(requestDto.inventory());
        car.setDailyFee(requestDto.dailyFee());

        return car;
    }

    public static CarResponseDto createDefaultCarDtoSample() {
        CarResponseDto carDto = new CarResponseDto();
        carDto.setId(DEFAULT_ID_SAMPLE);
        carDto.setModel(CAR_MODEL);
        carDto.setBrand(CAR_BRAND);
        carDto.setType(CAR_TYPE.name());
        carDto.setInventory(CAR_INVENTORY);
        carDto.setDailyFee(CAR_DAILY_FEE);

        return carDto;
    }

    public static CarResponseDto createCarDtoSampleFromEntity(Car car) {
        CarResponseDto carDto = new CarResponseDto();
        carDto.setId(car.getId());
        carDto.setModel(car.getModel());
        carDto.setBrand(car.getBrand());
        carDto.setType(car.getType().name());
        carDto.setInventory(car.getInventory());
        carDto.setDailyFee(car.getDailyFee());

        return carDto;
    }

    public static CarPreviewResponseDto createDefaultCarPreviewDtoSample() {
        CarPreviewResponseDto carDto = new CarPreviewResponseDto();
        carDto.setId(DEFAULT_ID_SAMPLE);
        carDto.setModel(CAR_MODEL);
        carDto.setBrand(CAR_BRAND);
        carDto.setType(CAR_TYPE.name());
        carDto.setDailyFee(CAR_DAILY_FEE);

        return carDto;
    }

    public static CarPreviewResponseDto createCarPreviewDtoSampleFromEntity(Car car) {
        CarPreviewResponseDto carDto = new CarPreviewResponseDto();
        carDto.setId(car.getId());
        carDto.setModel(car.getModel());
        carDto.setBrand(car.getBrand());
        carDto.setType(car.getType().name());
        carDto.setDailyFee(car.getDailyFee());

        return carDto;
    }
}
