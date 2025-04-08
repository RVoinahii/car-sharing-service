package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.CarDto;
import com.carshare.rentalsystem.dto.CarPreviewDto;
import com.carshare.rentalsystem.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.InventoryUpdateDto;
import com.carshare.rentalsystem.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car management", description = "Endpoints for managing cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {
    public static final String MODEL = "model";
    public static final String BRAND = "brand";

    private final CarService carService;

    @GetMapping
    @Operation(
            summary = "Get all cars",
            description = "Get a paginated list of all available cars in the inventory "
                    + "(No authentication required.)"
    )
    public Page<CarPreviewDto> getAllCars(@ParameterObject @PageableDefault(sort = {MODEL, BRAND},
            direction = Sort.Direction.ASC) Pageable pageable) {
        return carService.getAll(pageable);
    }

    @GetMapping("/{carId}")
    @Operation(
            summary = "Get a car by ID",
            description = "Get car by the given ID (No authentication required.)"
    )
    public CarDto getCarById(@PathVariable Long carId) {
        return carService.getById(carId);
    }

    @PostMapping
    @Operation(
            summary = "Create a new car",
            description = "Create a new car with the provided parameters (Required roles: ADMIN)"
    )
    public CarDto createCar(@RequestBody @Valid CreateCarRequestDto carDto) {
        return carService.create(carDto);
    }

    @PutMapping("/{carId}")
    @Operation(
            summary = "Update car by ID",
            description = "Update car by the given ID with the provided parameters "
                    + "(Required roles: ADMIN)"
    )
    public CarDto updateCar(@PathVariable Long carId,
                            @RequestBody @Valid CreateCarRequestDto carDto) {
        return carService.updateCarById(carId, carDto);
    }

    @PatchMapping("/{carId}")
    @Operation(
            summary = "Update car inventory by car ID",
            description = "Change th number of available cars in inventory by the given car"
                    + "ID with the provided parameters "
                    + "(Required roles: ADMIN)"
    )
    public CarDto updateCarInventory(@PathVariable Long carId,
                                     @RequestBody @Valid InventoryUpdateDto inventoryDto) {
        return carService.updateInventoryByCarId(carId, inventoryDto);
    }

    @DeleteMapping("/{carId}")
    @Operation(
            summary = "Delete a car by ID",
            description = "Mark a car as deleted by the given ID "
                    + "(Required roles: ADMIN)"
    )
    public void deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
    }
}
