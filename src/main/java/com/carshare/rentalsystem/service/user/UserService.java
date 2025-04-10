package com.carshare.rentalsystem.service.user;

import com.carshare.rentalsystem.dto.user.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.UserResponseDto;
import com.carshare.rentalsystem.dto.user.UserUpdateRequestDto;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto registrationRequestDto);

    UserResponseDto getUserInfo(Long userId);

    UserResponseDto updateUserInfo(Long userId, UserUpdateRequestDto updateRequestDto);

    UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto roleUpdateRequestDto);
}
