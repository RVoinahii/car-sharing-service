package com.carshare.rentalsystem.mapper;

import com.carshare.rentalsystem.config.MapperConfig;
import com.carshare.rentalsystem.dto.user.request.dto.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.model.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
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

    @Named("userToPreviewDto")
    @Mapping(target = "fullName",
            expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserPreviewResponseDto toPreviewDto(User user);

    void updateUserFromDto(UserUpdateRequestDto updatedUser, @MappingTarget User existingUser);
}
