package com.carshare.rentalsystem.controller;

import com.carshare.rentalsystem.dto.rental.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.RentalDetailsResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalSearchParameters;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental management", description = "Endpoints for managing rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {
    private static final String AUTHORITY_MANAGER = "MANAGER";

    private final RentalService rentalService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(
            summary = "Retrieve rentals",
            description = "Returns a paginated list of rental records. Customers can only view"
                    + " their own rentals. Managers can view all rentals and filter them using"
                    + " parameters such as activity status. (Required roles: CUSTOMER, MANAGER)"
    )
    public Page<RentalDetailsResponseDto> getAllRentals(Authentication authentication,
            RentalSearchParameters searchParameters, Pageable pageable) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AUTHORITY_MANAGER));

        if (isAdmin) {
            return rentalService.getSpecificRentals(searchParameters, pageable);
        }

        return rentalService.getRentalsById(getAuthenticatedUserId(authentication), pageable);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/{rentId}")
    @Operation(
            summary = "Get rental details by ID",
            description = "Returns detailed information about a specific rental by its ID."
                    + "Customers can only access their own rentals, while managers can access"
                    + " any rental. (Required roles: CUSTOMER, MANAGER)"
    )
    public RentalDetailsResponseDto getRentalDetails(Authentication authentication,
            @PathVariable Long rentId) {
        return rentalService.getRentalInfo(getAuthenticatedUserId(authentication), rentId);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping
    @Operation(
            summary = "Create a new rental",
            description = "Creates a new rental using the authenticated user's ID and the"
                    + " provided rental parameters. Also decreases the inventory of the rented car"
                    + " (Required roles: CUSTOMER, MANAGER)"
    )
    public RentalResponseDto rentCar(Authentication authentication,
            @RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.create(getAuthenticatedUserId(authentication), requestDto);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping("/{rentId}/returns")
    @Operation(
            summary = "Return a rented car",
            description = "Marks a rental as returned by setting the actual return date to the"
                    + " current date. Also increases the inventory count of the returned car."
                    + "(Required roles: CUSTOMER, MANAGER)"
    )
    public RentalDetailsResponseDto returnRental(Authentication authentication,
            @PathVariable Long rentId) {
        return rentalService.returnRental(getAuthenticatedUserId(authentication), rentId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
