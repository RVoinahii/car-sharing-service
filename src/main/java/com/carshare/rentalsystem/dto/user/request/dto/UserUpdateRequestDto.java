package com.carshare.rentalsystem.dto.user.request.dto;

import com.carshare.rentalsystem.annotations.AtLeastOneFieldNotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@AtLeastOneFieldNotEmpty
public record UserUpdateRequestDto(
        @Email
        String email,

        @Pattern(regexp = ".*\\S.*", message = "First name must not be blank")
        String firstName,

        @Pattern(regexp = ".*\\S.*", message = "Last name must not be blank")
        String lastName
) {

}
