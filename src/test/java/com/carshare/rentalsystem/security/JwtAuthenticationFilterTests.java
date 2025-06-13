package com.carshare.rentalsystem.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTests {
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserDetails userDetails;

    @Test
    @DisplayName("""
            doFilterInternal():
             Should authenticate user with valid token
            """)
    void doFilterInternal_validToken() throws Exception {
        //Given
        String validToken = "validToken";
        String username = "valid@gmail.com";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtUtil.isValidToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsername(validToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        //When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        //Then
        verify(jwtUtil).isValidToken(validToken);
        verify(jwtUtil).getUsername(validToken);
        verify(userDetailsService).loadUserByUsername(username);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("""
            doFilterInternal():
             Should pass the request when no token is provided
            """)
    void doFilterInternal_noToken() throws Exception {
        //Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        //When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        //Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("""
            doFilterInternal():
             Should not authenticate when token is invalid or missing
            """)
    void doFilterInternal_invalidOrMissingToken() throws Exception {
        //Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        //When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        //Then
        verify(jwtUtil, never()).isValidToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("""
            doFilterInternal():
             Should not authenticate when token is invalid
            """)
    void doFilterInternal_invalidToken() throws Exception {
        //Given
        String invalidToken = "invalidToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + invalidToken);
        when(jwtUtil.isValidToken(invalidToken)).thenReturn(false);

        //When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        //Then
        verify(jwtUtil).isValidToken(invalidToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("""
            doFilterInternal():
             Should authenticate user and set authentication in SecurityContext when token is valid
            """)
    void doFilterInternal_validToken_shouldSetAuthentication() throws Exception {
        // Given
        String validToken = "validToken";
        String username = "valid@gmail.com";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtUtil.isValidToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsername(validToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).isValidToken(validToken);
        verify(jwtUtil).getUsername(validToken);
        verify(userDetailsService).loadUserByUsername(username);
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
    }
}
