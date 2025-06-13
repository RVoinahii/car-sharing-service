package com.carshare.rentalsystem.service.user;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.DEFAULT_ID_SAMPLE;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.USER_EMAIL;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.USER_HASHED_PASSWORD;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserResponseDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserUpdateRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createRegistrationRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUpdateUserRoleRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUpdatedUserFromRequestDto;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUpdatedUserUpdateRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUserResponseDtoSampleFromEntity;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUserSampleFromRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.dto.user.request.dto.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.exception.EmailAlreadyExistsException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.RegistrationException;
import com.carshare.rentalsystem.mapper.UserMapper;
import com.carshare.rentalsystem.model.Role;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.role.RoleRepository;
import com.carshare.rentalsystem.repository.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("""
            register():
             Should return UserResponseDto when valid registration request is provided
            """)
    void register_ValidRegistrationRequest_ReturnResponseDto() {
        //Given
        UserRegistrationRequestDto userRegistrationRequestDto =
                createRegistrationRequestDtoSample();

        Long expectedUserId = 1L;

        User user = createUserSampleFromRequest(userRegistrationRequestDto);
        user.setId(expectedUserId);

        Role userRole = new Role();
        userRole.setRole(Role.RoleName.CUSTOMER);

        UserResponseDto expectedResponseDto = createUserResponseDtoSampleFromEntity(user);

        when(userMapper.toEntity(userRegistrationRequestDto)).thenReturn(user);
        when(passwordEncoder.encode(userRegistrationRequestDto.getPassword()))
                .thenReturn(USER_HASHED_PASSWORD);
        when(roleRepository.findByRole(Role.RoleName.CUSTOMER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedResponseDto);

        //When
        UserResponseDto actualResponseDto = userService.register(userRegistrationRequestDto);

        //Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);
        verify(userMapper).toEntity(userRegistrationRequestDto);
        verify(userRepository).existsByEmail(userRegistrationRequestDto.getEmail());
        verify(passwordEncoder).encode(userRegistrationRequestDto.getPassword());
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("""
            register():
             Should throw RegistrationException when email is already registered
            """)
    void register_EmailAlreadyExists_ShouldThrowException() {
        //Given
        UserRegistrationRequestDto userRegistrationRequestDto =
                createRegistrationRequestDtoSample();

        when(userRepository.existsByEmail(userRegistrationRequestDto.getEmail())).thenReturn(true);

        //When
        Exception exception = assertThrows(
                RegistrationException.class, () -> userService.register(userRegistrationRequestDto)
        );

        //Then
        String expected = "User with email:" + userRegistrationRequestDto.getEmail()
                + " is already exist";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(userRepository).existsByEmail(userRegistrationRequestDto.getEmail());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("""
            register():
             Should throw EntityNotFoundException when role is not found
            """)
    void register_RoleNotFound_ShouldThrowException() {
        //Given
        UserRegistrationRequestDto userRegistrationRequestDto =
                createRegistrationRequestDtoSample();

        User user = createUserSampleFromRequest(userRegistrationRequestDto);

        when(userMapper.toEntity(userRegistrationRequestDto)).thenReturn(user);
        when(passwordEncoder.encode(userRegistrationRequestDto.getPassword()))
                .thenReturn(USER_HASHED_PASSWORD);
        when(roleRepository.findByRole(Role.RoleName.CUSTOMER)).thenReturn(Optional.empty());

        //Then
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.register(userRegistrationRequestDto)
        );

        //Then
        String expected = "Role with name '" + Role.RoleName.CUSTOMER + "' not found";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(userRepository).existsByEmail(userRegistrationRequestDto.getEmail());
        verify(userMapper).toEntity(userRegistrationRequestDto);
        verify(passwordEncoder).encode(userRegistrationRequestDto.getPassword());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("""
        getUserInfo():
         Should return UserResponseDto when user exists
            """)
    void getUserInfo_UserExists_ReturnsUserResponseDto() {
        //Given
        User user = createDefaultUserSample();
        UserResponseDto expectedDto = createDefaultUserResponseDtoSample();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        //When
        UserResponseDto actualDto = userService.getUserInfo(user.getId());

        //Then
        assertThat(actualDto).isEqualTo(expectedDto);
        verify(userRepository).findById(user.getId());
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("""
        getUserInfo():
         Should throw EntityNotFoundException when user does not exist
            """)
    void getUserInfo_UserNotFound_ThrowsException() {
        //Given
        Long userId = 100L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserInfo(userId)
        );

        assertEquals("Can't find user by id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("""
    updateUserInfo():
     Should update user info and return updated UserResponseDto
            """)
    void updateUserInfo_ValidRequest_UpdatesAndReturnsDto() {
        //Given
        Long userId = DEFAULT_ID_SAMPLE;
        User existingUser = createDefaultUserSample();
        UserUpdateRequestDto requestDto = createUpdatedUserUpdateRequestDtoSample();
        UserResponseDto expectedDto = createUpdatedUserFromRequestDto(requestDto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userMapper).updateUserFromDto(requestDto, existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDto(existingUser)).thenReturn(expectedDto);

        //When
        UserResponseDto actualDto = userService.updateUserInfo(userId, requestDto);

        //Then
        assertEquals(expectedDto, actualDto);
        verify(userRepository).findById(userId);
        verify(userMapper).updateUserFromDto(requestDto, existingUser);
        verify(userRepository).save(existingUser);
        verify(userMapper).toDto(existingUser);
    }

    @Test
    @DisplayName("""
    updateUserInfo():
     Should throw EmailAlreadyExistsException when email already exists
            """)
    void updateUserInfo_EmailExists_ThrowsException() {
        //Given
        Long userId = DEFAULT_ID_SAMPLE;
        User existingUser = createDefaultUserSample();
        UserUpdateRequestDto requestDto = createDefaultUserUpdateRequestDtoSample();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);

        //When & Then
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.updateUserInfo(userId, requestDto)
        );

        assertEquals("Email: " + USER_EMAIL + " is already taken.", exception.getMessage());
        verify(userRepository).existsByEmail(requestDto.email());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("""
    updateUserRole():
     Should update user role and return updated UserResponseDto
            """)
    void updateUserRole_ValidRole_UpdatesAndReturnsDto() {
        //Given
        Long userId = DEFAULT_ID_SAMPLE;
        User user = createDefaultUserSample();
        Role.RoleName newRole = Role.RoleName.MANAGER;
        Role roleEntity = new Role();
        roleEntity.setRole(newRole);

        UpdateUserRoleRequestDto requestDto = createUpdateUserRoleRequestDtoSample(newRole);
        UserResponseDto expectedDto = createDefaultUserResponseDtoSample();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByRole(newRole)).thenReturn(Optional.of(roleEntity));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        //When
        UserResponseDto actualDto = userService.updateUserRole(userId, requestDto);

        //Then
        assertEquals(expectedDto, actualDto);
        verify(userRepository).findById(userId);
        verify(roleRepository).findByRole(newRole);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("""
    updateUserRole():
     Should throw EntityNotFoundException when role is not found
            """)
    void updateUserRole_RoleNotFound_ThrowsException() {
        //Given
        Long userId = DEFAULT_ID_SAMPLE;
        User user = createDefaultUserSample();
        Role.RoleName newRole = Role.RoleName.MANAGER;
        UpdateUserRoleRequestDto requestDto = createUpdateUserRoleRequestDtoSample(newRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByRole(newRole)).thenReturn(Optional.empty());

        //When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserRole(userId, requestDto)
        );

        assertEquals("Role with name 'MANAGER' not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(roleRepository).findByRole(newRole);
    }
}
