package com.carshare.rentalsystem.controller;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserResponseDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createRegistrationRequestDtoSample;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carshare.rentalsystem.dto.user.request.dto.UserLoginRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserRegistrationRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTests {
    protected static MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("""
            registerUser():
             Confirming the successful creation of a new user with a valid request
            """)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerUser_ValidRequestDto_Created() throws Exception {
        //Given
        UserRegistrationRequestDto requestDto = createRegistrationRequestDtoSample();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        UserResponseDto expectedUserResponseDto = createDefaultUserResponseDtoSample();

        //When
        MvcResult result = mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        UserResponseDto actualUserResponseDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDto.class);

        assertNotNull(actualUserResponseDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualUserResponseDto,
                expectedUserResponseDto, "id", "role"));
    }

    @Test
    @DisplayName("""
            registerUser():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    void registerUser_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("""
            login():
             Verifying the retrieval of the correct JWT token when provided with valid credentials
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void login_ValidCredentials_ReturnsJwtToken() throws Exception {
        //Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "mail.example@gmail.com", "password"
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
    }

    @Test
    @DisplayName("""
            login():
             Should return 401 UNAUTHORIZED when given invalid credentials
            """)
    void login_InvalidCredentials_Unauthorized() throws Exception {
        //Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "invalid.example@gmail.com", "invalid password"
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    @DisplayName("""
            login():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    void login_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        UserLoginRequestDto invalidRequestDto = new UserLoginRequestDto(
                null, null
        );

        String jsonRequest = objectMapper.writeValueAsString(invalidRequestDto);

        //When & Then
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
