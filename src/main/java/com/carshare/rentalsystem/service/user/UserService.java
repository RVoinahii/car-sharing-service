package com.carshare.rentalsystem.service.user;

import com.carshare.rentalsystem.dto.user.request.dto.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto registrationRequestDto);

    UserResponseDto getUserInfo(Long userId);

    UserResponseDto updateUserInfo(Long userId, UserUpdateRequestDto updateRequestDto);

    UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto roleUpdateRequestDto);
}
