package com.carshare.rentalsystem.security;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.USER_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTests {
    private static final String SECRET_KEY = "securesecuresecuresecuresecuresecure";
    private static final long EXPIRATION_TIME = 100000L;

    private JwtUtil jwtUtil;
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_TIME);

        validToken = jwtUtil.generateToken(USER_EMAIL);

        expiredToken = Jwts.builder()
                .subject(USER_EMAIL)
                .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION_TIME - 1000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    @DisplayName("""
            generateToken():
             Should return a valid token when a valid email is provided
            """)
    void generateToken_ShouldReturnValidToken() {
        //When
        String actualToken = jwtUtil.generateToken(USER_EMAIL);

        //Then
        assertNotNull(actualToken);
        assertFalse(actualToken.isEmpty());
    }

    @Test
    @DisplayName("""
            isValidToken():
             Should return true when a valid token is provided
            """)
    void isValidToken_ValidToken_ShouldReturnTrue() {
        //Given
        String token = validToken;

        //When & Then
        assertTrue(jwtUtil.isValidToken(validToken));
    }

    @Test
    @DisplayName("""
            isValidToken():
             Should throw JwtException when an expired token is provided
            """)
    void isValidToken_ExpiredToken_ShouldThrowException() {
        //Given
        String token = expiredToken;

        //When & Then
        assertThrows(JwtException.class, () -> jwtUtil.isValidToken(expiredToken));
    }

    @Test
    @DisplayName("""
            isValidToken():
             Should throw JwtException when an invalid token is provided
            """)
    void isValidToken_InvalidToken_ShouldThrowException() {
        //Given
        String token = "invalidToken";

        //When & Then
        assertThrows(JwtException.class, () -> jwtUtil.isValidToken(token));
    }

    @Test
    @DisplayName("""
            getUsername():
             Should return email when a valid token is provided
            """)
    void getUsername_ValidToken_ShouldReturnEmail() {
        //Given
        String token = validToken;

        //When
        String username = jwtUtil.getUsername(token);

        //Then
        assertEquals(USER_EMAIL, username);
    }

    @Test
    @DisplayName("""
            getUsername():
             Should throw JwtException when an invalid token is provided
            """)
    void getUsername_InvalidToken_ShouldThrowException() {
        //Given
        String token = "invalidToken";

        //When & Then
        assertThrows(JwtException.class, () -> jwtUtil.getUsername(token));
    }
}
