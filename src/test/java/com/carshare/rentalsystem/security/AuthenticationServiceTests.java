package com.carshare.rentalsystem.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.dto.user.request.dto.UserLoginRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserLoginResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("""
            authenticate():
             Should return UserLoginResponseDto with valid JWT token when credentials are correct
            """)
    void authenticate_ValidCredentials_ReturnsJwtToken() {
        //Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "valid@gmail.com", "validPassword");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn(requestDto.email());
        when(jwtUtil.generateToken(requestDto.email())).thenReturn("mocked_jwt_token");

        //When
        UserLoginResponseDto response = authenticationService.authenticate(requestDto);

        //Then
        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(requestDto.email());
    }

    @Test
    @DisplayName("""
            authenticate():
             Should throw BadCredentialsException when invalid credentials are provided
            """)
    void authenticate_InvalidCredentials_ThrowsException() {
        //Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "invalid@gmail.com", "invalidPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        //When
        Exception exception = assertThrows(
                BadCredentialsException.class, () -> authenticationService.authenticate(requestDto)
        );

        //Then
        String expected = "Bad credentials";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtil);
    }
}
