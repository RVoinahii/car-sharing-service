package com.carshare.rentalsystem;

import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.service.CarService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@SpringBootApplication
public class CarShareApplication {
    private static final Long TESLA_MODEL_3_ID = 1L;
    private static final Long AUDI_Q7_ID = 2L;

    private final CarService carService;

    public static void main(String[] args) {
        SpringApplication.run(CarShareApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            List<CreateCarRequestDto> sampleCars = createSampleCars();
            sampleCars.forEach(carService::create);

            carService.deleteById(AUDI_Q7_ID);

            System.out.println(carService.getAll());

            CreateCarRequestDto updatedCarOne = new CreateCarRequestDto(
                    "Model 3", "Tesla", Car.Type.SEDAN, 10, new BigDecimal("50.00")
            );

            carService.updateById(TESLA_MODEL_3_ID, updatedCarOne);

            System.out.println(carService.getById(TESLA_MODEL_3_ID));
        };
    }

    private List<CreateCarRequestDto> createSampleCars() {
        List<CreateCarRequestDto> cars = new ArrayList<>();

        cars.add(new CreateCarRequestDto(
                "Model 3", "Tesla", Car.Type.SEDAN, 15, new BigDecimal("40.00")
        ));

        cars.add(new CreateCarRequestDto(
                "Q7", "Audi", Car.Type.SUV, 8, new BigDecimal("60.00")
        ));

        cars.add(new CreateCarRequestDto(
                "Civic", "Honda", Car.Type.HATCHBACK, 5, new BigDecimal("30.00")
        ));

        cars.add(new CreateCarRequestDto(
                "Passat Variant", "Volkswagen", Car.Type.UNIVERSAL, 7, new BigDecimal("35.00")
        ));

        return cars;
    }
}
