package com.carshare.rentalsystem.service.user;

import com.carshare.rentalsystem.dto.user.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.UserResponseDto;
import com.carshare.rentalsystem.dto.user.UserUpdateRequestDto;
import com.carshare.rentalsystem.exception.EmailAlreadyExistsException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.RegistrationException;
import com.carshare.rentalsystem.mapper.UserMapper;
import com.carshare.rentalsystem.model.Role;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.role.RoleRepository;
import com.carshare.rentalsystem.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User with email: " + requestDto.getEmail()
                    + "is already exist");
        }
        User user = createUser(requestDto);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getUserInfo(Long userId) {
        return userMapper.toDto(findUserById(userId));
    }

    @Override
    public UserResponseDto updateUserInfo(Long userId, UserUpdateRequestDto requestDto) {
        User existingUser = findUserById(userId);
        System.out.println(existingUser);
        if (requestDto.email() != null) {
            if (userRepository.existsByEmail(requestDto.email())) {
                throw new EmailAlreadyExistsException(
                        "Email: " + requestDto.email() + " is already taken.");
            }

        }
        userMapper.updateUserFromDto(requestDto, existingUser);
        System.out.println(existingUser);
        return userMapper.toDto(userRepository.save(existingUser));
    }

    @Override
    public UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto requestDto) {
        User existingUser = findUserById(userId);
        Role role = findByRole(requestDto.role());
        existingUser.setRole(role);
        return userMapper.toDto(userRepository.save(existingUser));
    }

    private User createUser(UserRegistrationRequestDto requestDto) {
        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        Role role = findByRole(Role.RoleName.CUSTOMER);
        user.setRole(role);
        return userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: "
                        + userId));
    }

    private Role findByRole(Role.RoleName roleName) {
        return roleRepository.findByRole(roleName).orElseThrow(
                () -> new EntityNotFoundException(
                        "Role with name '" + roleName.name() + "' not found")
        );
    }
}
