package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Rental management",
        description = """
        Endpoints for managing the full rental lifecycle of cars.

        - **Customers** can create rentals, return cars, and view their rental history.
        - **Managers** can access and filter all rentals across users.

        Business logic includes:
        - Car inventory adjustments upon rental/return
        - Status transitions (e.g. `RESERVED`, `ACTIVE`, `WAITING_FOR_PAYMENT`, `CANCELLED`)
        - Rental restrictions (e.g. max active rentals per user, early cancellation rules)
            """
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {
    public static final String AUTHORITY_MANAGER = "MANAGER";

    private final RentalService rentalService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(
            summary = "Retrieve rentals",
            description = """
                Returns a paginated list of rental records.
                - Customers can view only their **own** rentals.
                - Managers can view **all** rentals and filter them by parameters such as `userId`
                and `status`.
                
                **Required roles**: CUSTOMER, MANAGER
            """
    )
    public Page<RentalPreviewResponseDto> getAllRentals(Authentication authentication,
            RentalSearchParameters searchParameters, Pageable pageable) {
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AUTHORITY_MANAGER));

        if (isManager) {
            return rentalService.getSpecificRentals(searchParameters, pageable);
        }

        return rentalService.getRentalsById(getAuthenticatedUserId(authentication), pageable);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/{rentalId}")
    @Operation(
            summary = "Get rental details by ID",
            description = """
                Retrieves detailed information about a specific rental by its ID.
                - Customers can access only their **own** rentals.
                - Managers can access **any** rental.
            
                **Required roles**: CUSTOMER, MANAGER
            """
    )
    public RentalResponseDto getRentalDetails(Authentication authentication,
                                              @PathVariable Long rentalId) {
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AUTHORITY_MANAGER));

        if (isManager) {
            return rentalService.getAnyRentalInfo(rentalId);
        }

        return rentalService.getCustomerRentalInfo(
                getAuthenticatedUserId(authentication), rentalId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping
    @Operation(
            summary = "Create a new rental",
            description = """
                Creates a new rental for the authenticated user with the provided parameters.
        
                Business rules:
                - Maximum rental duration is 14 days.
                - If rentalDate is in the future, the rental status is set to RESERVED.
                - If rentalDate is today or in the past, the rental status is set to ACTIVE.
                - The inventory count of the rented car is decreased by one.
                - A user can have at most 3 active rentals (statuses 'RESERVED',
                 'WAITING_FOR_PAYMENT', 'ACTIVE').
        
                **Required roles**: CUSTOMER, MANAGER
            """
    )
    public RentalResponseDto rentCar(Authentication authentication,
                                     @RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.create(getAuthenticatedUserId(authentication), requestDto);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping(value = "/{rentId}/returns")
    @Operation(
            summary = "Return a rented car",
            description = """
                Marks a rental as returned by:
                - Setting the actual return date to the current date.
                - Updating the rental status according to business rules:
                  * If the rental was 'RESERVED' and cancelled early enough, status changes to
                   'CANCELLED'.
                  * Otherwise, status changes to 'WAITING_FOR_PAYMENT'.
                - Increasing the inventory count of the returned car by one.
        
                Cancellation of 'RESERVED' rentals must happen at least 3 days before the rental
                 start date.

                **Required roles**: CUSTOMER, MANAGER
            """
    )
    public RentalResponseDto returnRental(Authentication authentication,
                                          @PathVariable Long rentId) {
        return rentalService.returnRental(getAuthenticatedUserId(authentication), rentId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
