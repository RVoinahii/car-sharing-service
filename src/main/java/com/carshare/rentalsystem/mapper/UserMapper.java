package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.user.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.UserResponseDto;
import com.carshare.rentalsystem.dto.user.UserUpdateRequestDto;
import com.carshare.rentalsystem.model.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "role", ignore = true)
    UserResponseDto toDto(User user);

    @AfterMapping
    default void setRole(@MappingTarget UserResponseDto userDto, User user) {
        userDto.setRole(user.getRole().getRole().name());
    }

    User toEntity(UserRegistrationRequestDto registrationRequestDto);

    void updateUserFromDto(UserUpdateRequestDto updatedUser, @MappingTarget User existingUser);
}
