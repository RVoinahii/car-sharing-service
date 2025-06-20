package com.carshare.rentalsystem.test.util;

import com.carshare.rentalsystem.dto.user.request.dto.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.model.Role;
import com.carshare.rentalsystem.model.User;

public class TestUserDataUtil {
    public static final String USER_EMAIL = "mail.example@gmail.com";
    public static final String USER_WITHOUT_RENTALS_EMAIL = "withoutrentals.example@gmail.com";
    public static final String USER_PASSWORD = "password";
    public static final String USER_HASHED_PASSWORD =
            "$2a$10$40VcEX9yypgQXTJ0CL/oteEJuB03CJ0lGzxmhB1ZlsDDLL5LnHbga";
    public static final Long DEFAULT_ID_SAMPLE = 1L;
    public static final String USER_FIRST_NAME = "FirstName";
    public static final String USER_LAST_NAME = "LastName";
    public static final String CUSTOMER_AUTHORITY = "CUSTOMER";
    public static final String MANAGER_AUTHORITY = "MANAGER";

    public static final String UPDATED_EMAIL = "updated.mail@example.com";
    public static final String UPDATED_FIRST_NAME = "UpdatedFirstName";
    public static final String UPDATED_LAST_NAME = "UpdatedLastName";

    public static UserRegistrationRequestDto createRegistrationRequestDtoSample() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail(USER_EMAIL);
        requestDto.setPassword(USER_PASSWORD);
        requestDto.setRepeatPassword(USER_PASSWORD);
        requestDto.setFirstName(USER_FIRST_NAME);
        requestDto.setLastName(USER_LAST_NAME);
        return requestDto;
    }

    public static UpdateUserRoleRequestDto createUpdateUserRoleRequestDtoSample(
            Role.RoleName roleName) {
        return new UpdateUserRoleRequestDto(roleName);
    }

    public static UserUpdateRequestDto createDefaultUserUpdateRequestDtoSample() {
        return new UserUpdateRequestDto(USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME);
    }

    public static UserUpdateRequestDto createUpdatedUserUpdateRequestDtoSample() {
        return new UserUpdateRequestDto(UPDATED_EMAIL, UPDATED_FIRST_NAME, UPDATED_LAST_NAME);
    }

    public static User createUserSampleFromRequest(UserRegistrationRequestDto requestDto) {
        User user = new User();
        user.setId(DEFAULT_ID_SAMPLE);
        user.setEmail(requestDto.getEmail());
        user.setPassword(USER_HASHED_PASSWORD);
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());

        Role role = new Role();
        role.setId(DEFAULT_ID_SAMPLE);
        role.setRole(Role.RoleName.CUSTOMER);

        user.setRole(role);
        return user;
    }

    public static User createDefaultUserSample() {
        User user = new User();
        user.setId(DEFAULT_ID_SAMPLE);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_HASHED_PASSWORD);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);

        Role role = new Role();
        role.setId(DEFAULT_ID_SAMPLE);
        role.setRole(Role.RoleName.CUSTOMER);

        user.setRole(role);
        return user;
    }

    public static UserResponseDto createUpdatedUserFromRequestDto(UserUpdateRequestDto requestDto) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setEmail(requestDto.email());
        responseDto.setFirstName(requestDto.firstName());
        responseDto.setLastName(requestDto.lastName());
        return responseDto;
    }

    public static UserResponseDto createUserResponseDtoSampleFromEntity(
            User user) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(user.getId());
        responseDto.setEmail(user.getEmail());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());
        return responseDto;
    }

    public static UserResponseDto createDefaultUserResponseDtoSample() {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(DEFAULT_ID_SAMPLE);
        responseDto.setEmail(USER_EMAIL);
        responseDto.setFirstName(USER_FIRST_NAME);
        responseDto.setLastName(USER_LAST_NAME);
        return responseDto;
    }
}
