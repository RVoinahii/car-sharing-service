package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.car.request.dto.CarSearchParameters;
import com.carshare.rentalsystem.dto.car.request.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.request.dto.InventoryUpdateRequestDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarResponseDto;
import com.carshare.rentalsystem.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Car management",
        description = """
        Endpoints for viewing, creating, updating, and deleting car records.

        - **Public access** to view available cars and their details
        - **Manager-only** access to add new cars, update information or inventory, and
         soft-delete cars

        Supports pagination and sorting for car listings.
            """
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {
    public static final String MODEL = "model";
    public static final String BRAND = "brand";

    private final CarService carService;

    @GetMapping
    @Operation(
            summary = "Retrieve all available cars",
            description = """
        Returns a paginated list of **all** available cars in the inventory.
        Supports sorting by `model` and `brand`.
        Each item includes: car ID, model, brand, type, and daily rental fee.
        
        **No authentication required.**
            """
    )
    public Page<CarPreviewResponseDto> getAllCars(CarSearchParameters searchParameters,
            @ParameterObject @PageableDefault(sort = {MODEL, BRAND},
                    direction = Sort.Direction.ASC) Pageable pageable) {
        return carService.getAll(searchParameters, pageable);
    }

    @GetMapping("/{carId}")
    @Operation(
            summary = "Get detailed car info by ID",
            description = """
        Retrieves detailed information for a specific car by ID.
        Response includes: model, brand, type, inventory count, and daily rental fee.
        
        **No authentication required.**
            """
    )
    public CarResponseDto getCarById(@PathVariable Long carId) {
        return carService.getById(carId);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(
            summary = "Create a new car entry",
            description = """
        Adds a new car to the inventory.
        Required fields:
        - Model (string)
        - Brand (string)
        - Type (enum: e.g., `SUV`, `SEDAN`, etc.)
        - Inventory count (positive integer)
        - Daily rental fee (positive decimal)
        
        **Required roles**: MANAGER
            """
    )
    public CarResponseDto createCar(@RequestBody @Valid CreateCarRequestDto carDto) {
        return carService.create(carDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PatchMapping("/{carId}")
    @Operation(
            summary = "Update car details by ID",
            description = """
        Updates an existing car’s model, brand, type, inventory, or daily fee.
        All fields are required (same as for car creation).
        
        **Required roles**: MANAGER
            """
    )
    public CarResponseDto updateCar(@PathVariable Long carId,
            @RequestBody @Valid CreateCarRequestDto carDto) {
        return carService.updateCarById(carId, carDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{carId}")
    @Operation(
            summary = "Update car inventory only",
            description = """
        Changes only the number of available cars in inventory for a given car ID.
        Takes a single integer value (must be >= 0).
        
        **Required roles**: MANAGER
            """
    )
    public CarResponseDto updateCarInventory(@PathVariable Long carId,
            @RequestBody @Valid InventoryUpdateRequestDto inventoryDto) {
        return carService.updateInventoryByCarId(carId, inventoryDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{carId}")
    @Operation(
            summary = "Delete a car by ID",
            description = """
        Performs a **soft delete** of the specified car.
          - The car is not physically removed from the database.
          - Instead, it's marked as **inactive** and will no longer appear in active listings.
          - This allows data retention and potential recovery.
        
        **Required roles**: MANAGER
            """
    )
    public void deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
    }
}
