package com.carshare.rentalsystem.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTests {
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("""
            loadUserByUsername():
             should return user details when user exists
            """)
    void loadUserByUsername_ValidEmail_ReturnsUserDetails() {
        // Given
        String validEmail = "valid@gmail.com";
        User user = new User();
        user.setEmail(validEmail);

        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(validEmail);

        // Then
        assertNotNull(userDetails);
        assertEquals(validEmail, userDetails.getUsername());
        verify(userRepository).findByEmail(validEmail);
    }

    @Test
    @DisplayName("""
            loadUserByUsername():
             should throw EntityNotFoundException when user does not exist
            """)
    void loadUserByUsername_InvalidEmail_ThrowsException() {
        // Given
        String invalidEmail = "invalid@gmail.com";

        when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(
                EntityNotFoundException.class, () ->
                        customUserDetailsService.loadUserByUsername(invalidEmail)
        );
        String expected = "Can't find user by email:" + invalidEmail;
        String actual = exception.getMessage();

        assertEquals("Can't find user by email:" + invalidEmail, exception.getMessage());
        verify(userRepository).findByEmail(invalidEmail);
    }
}
