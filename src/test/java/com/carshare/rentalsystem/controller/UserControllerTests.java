package com.carshare.rentalsystem.controller;

import static com.carshare.rentalsystem.test.util.TestUserDataUtil.CUSTOMER_AUTHORITY;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.MANAGER_AUTHORITY;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.USER_EMAIL;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserResponseDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUpdatedUserFromRequestDto;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createUpdatedUserUpdateRequestDtoSample;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carshare.rentalsystem.client.telegram.TelegramAuthenticationService;
import com.carshare.rentalsystem.dto.telegram.TelegramTokenResponseDto;
import com.carshare.rentalsystem.dto.user.request.dto.UpdateUserRoleRequestDto;
import com.carshare.rentalsystem.dto.user.request.dto.UserUpdateRequestDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.model.Role;
import com.carshare.rentalsystem.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {
    protected static MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TelegramAuthenticationService telegramAuthenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDetailsService userDetailsService;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            getProfileInfo():
             Verifying retrieving profile info with valid authentication
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getProfileInfo_WithAuth_Success() throws Exception {
        //Given
        Long expectedUserId = 3L;

        UserResponseDto expectedUserDto = createDefaultUserResponseDtoSample();
        expectedUserDto.setId(expectedUserId);
        //When
        MvcResult result = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        UserResponseDto actualUserDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDto.class);
        assertNotNull(actualUserDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualUserDto, expectedUserDto, "role"));
    }

    @Test
    @DisplayName("""
            getProfileInfo():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void getProfileInfo_WithoutAuth_Unauthorized() throws Exception {
        //When & Then
        MvcResult result = mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    //loginToTelegram
    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            loginToTelegram():
             Verifying generating Telegram login link with valid authentication
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void loginToTelegram_Success() throws Exception {
        //Given
        Long expectedUserId = 3L;
        String expectedPrefix = "https://t.me/car_sharing_alert_bot?start=";

        //When
        MvcResult result = mockMvc.perform(get("/users/telegram"))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        TelegramTokenResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TelegramTokenResponseDto.class);

        assertNotNull(response);
        assertNotNull(response.token());
        assertTrue(response.token().startsWith(expectedPrefix));
    }

    @Test
    @DisplayName("""
            loginToTelegram():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void loginToTelegram_WithoutAuth_Unauthorized() throws Exception {
        //When & Then
        MvcResult result = mockMvc.perform(get("/users/telegram"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            updateProfileInfo():
             Verifying updating profile info with valid request
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateProfileInfo_ValidRequestDto_Success() throws Exception {
        //Given
        Long expectedUserId = 3L;

        UserUpdateRequestDto requestDto = createUpdatedUserUpdateRequestDtoSample();
        UserResponseDto expectedUserDto = createUpdatedUserFromRequestDto(requestDto);
        expectedUserDto.setId(expectedUserId);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(patch("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        UserResponseDto actualUserDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDto.class);
        assertNotNull(actualUserDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualUserDto, expectedUserDto, "role"));
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            updateProfileInfo():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateProfileInfo_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(null, null, null);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(patch("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            updateProfileInfo():
            Should return 400 BAD REQUEST when the request body is missing
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateProfileInfo_WithoutRequestDto_BadRequest() throws Exception {
        //Given

        //When & Then
        MvcResult result = mockMvc.perform(patch("/users/me"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateUserRole():
             Verifying updating user role with valid request and manager authority
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_ValidRequestDtoAndAuthority_Success() throws Exception {
        //Given
        Long userId = 3L;

        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.RoleName.MANAGER);
        UserResponseDto expectedUserDto = createDefaultUserResponseDtoSample();
        expectedUserDto.setId(userId);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(put("/users/{userId}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        UserResponseDto actualUserDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDto.class);
        assertNotNull(actualUserDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualUserDto, expectedUserDto, "role"));
        assertEquals(actualUserDto.getRole(), requestDto.role().name());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateUserRole():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    void updateUserRole_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        Long userId = 3L;

        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(null);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(put("/users/{userId}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateUserRole():
             Should return 400 BAD REQUEST when the request body is missing
            """)
    void updateUserRole_WithoutRequestDto_BadRequest() throws Exception {
        //Given
        Long userId = 3L;

        //When & Then
        MvcResult result = mockMvc.perform(put("/users/{userId}/role", userId))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateUserRole():
             Should return 404 NOT FOUND when given invalid ID
            """)
    void updateUserRole_InvalidUserId_NotFound() throws Exception {
        //Given
        Long userId = 999L;

        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.RoleName.MANAGER);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(put("/users/{userId}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithMockUser(username = "customer", authorities = CUSTOMER_AUTHORITY)
    @Test
    @DisplayName("""
            updateUserRole():
             Should return 403 FORBIDDEN when user doesn't have authority 'MANAGER'
            """)
    void updateUserRole_InvalidAuthority_Forbidden() throws Exception {
        //Given
        Long userId = 3L;

        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.RoleName.MANAGER);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        MvcResult result = mockMvc.perform(put("/users/{userId}/role", userId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            updateUserRole():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void updateUserRole_WithoutAuth_Unauthorized() throws Exception {
        //Give
        Long userId = 3L;

        //When & Then
        MvcResult result = mockMvc.perform(put("/users/{userId}/role", userId))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
