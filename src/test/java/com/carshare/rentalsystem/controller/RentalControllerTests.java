package com.carshare.rentalsystem.controller;

import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_SIZE;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createActiveRentalDtoSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createCompletedRentalDtoSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createCompletedRentalPreviewDtoSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createRentalRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.MANAGER_AUTHORITY;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.USER_EMAIL;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.USER_WITHOUT_RENTALS_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.carshare.rentalsystem.test.util.TestPageImplDeserializerUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
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
public class RentalControllerTests {
    protected static MockMvc mockMvc;

    @Autowired
    private RentalService rentalService;

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

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            getAllRentals():
             Should return all rentals for manager, when valid pagination is provided
              and no search parameters are set
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllRentals_ManagerWithValidPageableWithoutParameters_Success() throws Exception {
        //Given
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new TestPageImplDeserializerUtil<>(
                RentalPreviewResponseDto.class, PAGE_SIZE));
        localMapper.registerModule(module);

        //When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("userId", "")
                        .param("status", "")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        PageImpl<RentalPreviewResponseDto> actualRentalDtosPage =
                localMapper.readValue(result.getResponse()
                        .getContentAsString(), new TypeReference<>() {});

        assertNotNull(actualRentalDtosPage);
        assertEquals(3, actualRentalDtosPage.getTotalElements());
        assertEquals(PAGE_SIZE, actualRentalDtosPage.getSize());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            getAllRentals():
             Should return rentals matching the search for manager when valid pagination
              and search parameters are provided
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllRentals_ValidPageableWithParameter_Success() throws Exception {
        //Given
        Long expectedId = 1L;
        Long expectedCarId = 1L;
        Long expectedUserId = 3L;
        String expectedStatus = Rental.RentalStatus.ACTIVE.name();

        RentalPreviewResponseDto expectedRentalDto = createCompletedRentalPreviewDtoSample();
        expectedRentalDto.setId(expectedId);
        expectedRentalDto.setCarId(expectedCarId);
        expectedRentalDto.setUserId(expectedUserId);
        expectedRentalDto.setStatus(expectedStatus);

        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new TestPageImplDeserializerUtil<>(
                RentalPreviewResponseDto.class, PAGE_SIZE));
        localMapper.registerModule(module);

        //When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .param("status", expectedStatus)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        PageImpl<RentalPreviewResponseDto> actualRentalDtosPage =
                localMapper.readValue(result.getResponse()
                        .getContentAsString(), new TypeReference<>() {});

        assertNotNull(actualRentalDtosPage);
        assertEquals(1, actualRentalDtosPage.getTotalElements());
        assertEquals(PAGE_SIZE, actualRentalDtosPage.getSize());
        assertTrue(EqualsBuilder.reflectionEquals(
                actualRentalDtosPage.getContent().getFirst(), expectedRentalDto));
    }

    @WithUserDetails(value = USER_EMAIL,
            userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            getAllRentals():
             Should return all rentals for customer, when valid pagination is provided
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllRentals_CustomerWithValidPageable_Success() throws Exception {
        //Given
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new TestPageImplDeserializerUtil<>(
                RentalPreviewResponseDto.class, PAGE_SIZE));
        localMapper.registerModule(module);

        //When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        PageImpl<RentalPreviewResponseDto> actualRentalDtosPage =
                localMapper.readValue(result.getResponse()
                        .getContentAsString(), new TypeReference<>() {});

        assertNotNull(actualRentalDtosPage);
        assertEquals(3, actualRentalDtosPage.getTotalElements());
        assertEquals(PAGE_SIZE, actualRentalDtosPage.getSize());
    }

    @WithUserDetails(value = USER_WITHOUT_RENTALS_EMAIL,
            userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            getAllRentals():
             Should return empty rentals page for customer without rentals
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user_without_rentals.sql",
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_rentals.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllRentals_CustomerWithoutRentals_Success() throws Exception {
        //Given
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new TestPageImplDeserializerUtil<>(
                RentalPreviewResponseDto.class, PAGE_SIZE));
        localMapper.registerModule(module);

        //When
        MvcResult result = mockMvc.perform(get("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        PageImpl<RentalPreviewResponseDto> actualRentalDtosPage =
                localMapper.readValue(result.getResponse()
                        .getContentAsString(), new TypeReference<>() {});

        assertNotNull(actualRentalDtosPage);
        assertEquals(0, actualRentalDtosPage.getTotalElements());
        assertEquals(PAGE_SIZE, actualRentalDtosPage.getSize());
    }

    @Test
    @DisplayName("""
            getAllRentals():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void getAllRentals_WithoutAuth_Unauthorized() throws Exception {
        //When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            getRentalDetails():
             Verifying retrieving any rental info by its ID with MANAGER role
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_completed_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRentalDetails_WithWithValidId_Success() throws Exception {
        //Given
        Long rentalId = 1L;

        RentalResponseDto expectedRentalDto = createCompletedRentalDtoSample();
        expectedRentalDto.setId(rentalId);
        expectedRentalDto.setRentalDate(LocalDate.now().minusDays(5));
        expectedRentalDto.setReturnDate(LocalDate.now().minusDays(2));
        expectedRentalDto.setActualReturnDate(LocalDate.now().minusDays(1));

        Long expectedRentalUserId = 3L;
        expectedRentalDto.getUser().setId(expectedRentalUserId);

        //When
        MvcResult result = mockMvc.perform(get("/rentals/{rentalId}", rentalId))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        RentalResponseDto actualRentalDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(actualRentalDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualRentalDto, expectedRentalDto,
                "car", "user"));
        assertTrue(actualRentalDto.getUser().getId().equals(expectedRentalUserId));
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            getRentalDetails():
             Should return 404 NOT FOUND when MANAGER and given invalid ID
            """)
    void getRentalDetails_ManagerWithInvalidId_NotFound() throws Exception {
        //Given
        Long rentalId = 99L;
        //When & Then
        mockMvc.perform(get("/rentals/{rentalId}", rentalId))
                .andExpect(status().isNotFound());
    }

    @WithUserDetails(value = USER_EMAIL,
            userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            getRentalDetails():
             Verifying retrieving any rental info by its ID with MANAGER role
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_completed_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRentalDetails_CustomerWithValidId_Success() throws Exception {
        //Given
        Long rentalId = 1L;

        RentalResponseDto expectedRentalDto = createCompletedRentalDtoSample();
        expectedRentalDto.setId(rentalId);
        expectedRentalDto.setRentalDate(LocalDate.now().minusDays(5));
        expectedRentalDto.setReturnDate(LocalDate.now().minusDays(2));
        expectedRentalDto.setActualReturnDate(LocalDate.now().minusDays(1));

        Long expectedRentalUserId = 3L;
        expectedRentalDto.getUser().setId(expectedRentalUserId);

        //When
        MvcResult result = mockMvc.perform(get("/rentals/{rentalId}", rentalId))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        RentalResponseDto actualRentalDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(actualRentalDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualRentalDto, expectedRentalDto,
                "car", "user"));
        assertTrue(actualRentalDto.getUser().getId().equals(expectedRentalUserId));
    }

    @WithUserDetails(value = USER_WITHOUT_RENTALS_EMAIL,
            userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            getRentalDetails():
             Should return 404 NOT FOUND when CUSTOMER and given invalid ID
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user_without_rentals.sql",
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_completed_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRentalDetails_CustomerWithInvalidId_NotFound() throws Exception {
        //Given
        Long rentalId = 1L;
        //When & Then
        mockMvc.perform(get("/rentals/{rentalId}", rentalId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("""
            getRentalDetails():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void getRentalDetails_WithoutAuth_Unauthorized() throws Exception {
        //Given
        Long rentalId = 1L;
        //When & Then
        mockMvc.perform(get("/rentals/{rentalId}", rentalId))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Confirming successful creation of a rental with valid request
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_ValidRequestDto_Created() throws Exception {
        //Given
        Long userId = 3L;

        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        RentalResponseDto expectedRentalDto = createActiveRentalDtoSample();
        expectedRentalDto.getUser().setId(userId);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        RentalResponseDto actualRentalDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(actualRentalDto);
        assertNotNull(actualRentalDto.getId());
        assertTrue(EqualsBuilder.reflectionEquals(actualRentalDto, expectedRentalDto,
                "car", "user"));
        assertTrue(actualRentalDto.getUser().getId().equals(userId));
        assertTrue(actualRentalDto.getCar().getId().equals(expectedRentalDto.getCar().getId()));
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_InvalidRequestDto_BadRequest() throws Exception {
        //When & Then
        mockMvc.perform(post("/rentals"))
                .andExpect(status().isBadRequest());
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Should return 404 NOT_FOUND when given invalid carId
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_InvalidCarId_BadRequest() throws Exception {
        //Given
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        requestDto.setCarId(999L);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Should return 400 BAD REQUEST when given invalid rental date
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_InvalidRentalDate_BadRequest() throws Exception {
        //Given
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        requestDto.setRentalDate(LocalDate.now().minusDays(99));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Should return 400 BAD REQUEST when given invalid return date
            """)
    @Sql(scripts = "classpath:database/users/insert_one_user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_InvalidReturnDate_BadRequest() throws Exception {
        //Given
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        requestDto.setReturnDate(LocalDate.now().minusDays(99));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Confirming successful creation of a rental with valid request
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_three_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_MoreThan3ActiveRentals_Conflict() throws Exception {
        //Given
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            rentCar():
             Confirming successful creation of a rental with valid request
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_out_of_stock_car.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void rentCar_CarIsOutOfStock_Conflict() throws Exception {
        //Given
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(
                        post("/rentals")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    @DisplayName("""
            rentCar():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void rentCar_WithoutAuth_Unauthorized() throws Exception {
        //Given
        Long rentalId = 1L;
        //When & Then
        mockMvc.perform(post("/rentals", rentalId))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            returnRental():
             Confirming successful creation of a rental with valid request
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void returnRental_ValidRentalId_Success() throws Exception {
        //Given
        RentalResponseDto expectedRentalDto = createCompletedRentalDtoSample();
        expectedRentalDto.setReturnDate(expectedRentalDto.getRentalDate().plusDays(2));
        expectedRentalDto.setActualReturnDate(LocalDate.now());
        expectedRentalDto.setStatus(Rental.RentalStatus.WAITING_FOR_PAYMENT.name());

        Long userId = 3L;
        expectedRentalDto.getUser().setId(userId);

        Long rentalId = 1L;

        //When
        MvcResult result = mockMvc.perform(
                        post("/rentals/{rentalId}/returns", rentalId))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        RentalResponseDto actualRentalDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalResponseDto.class);
        assertNotNull(actualRentalDto);
        assertNotNull(actualRentalDto.getId());
        assertTrue(EqualsBuilder.reflectionEquals(actualRentalDto, expectedRentalDto,
                "car", "user"));
        assertTrue(actualRentalDto.getUser().getId().equals(userId));
        assertTrue(actualRentalDto.getCar().getId().equals(expectedRentalDto.getCar().getId()));
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            returnRental():
             Should return 404 NOT_FOUND when given invalid rentalId
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void returnRental_InvalidRentalId_BadRequest() throws Exception {
        //Given
        Long rentalId = 1L;

        //When & Then
        mockMvc.perform(
                        post("/rentals/{rentalId}/returns", rentalId))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithUserDetails(value = USER_WITHOUT_RENTALS_EMAIL,
            userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            returnRental():
             Should return 403 FORBIDDEN when given invalid rentalId
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user_without_rentals.sql",
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_active_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void returnRental_InvalidRentalIdForCurrentUser_Forbidden() throws Exception {
        //Given
        Long rentalId = 1L;

        //When
        mockMvc.perform(
                        post("/rentals/{rentalId}/returns", rentalId))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            returnRental():
             Should return 409 CONFLICT when given already returned rental
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_completed_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void returnRental_RentalAlreadyReturn_BadRequest() throws Exception {
        //Given
        Long rentalId = 1L;

        //When & Then
        mockMvc.perform(
                        post("/rentals/{rentalId}/returns", rentalId))
                .andExpect(status().isConflict())
                .andReturn();
    }

    @WithUserDetails(value = USER_EMAIL, userDetailsServiceBeanName = "customUserDetailsService")
    @Test
    @DisplayName("""
            returnRental():
             Should return 400 BAD_REQUEST when is too late for cancel rental
            """)
    @Sql(scripts = {
            "classpath:database/users/insert_one_user.sql",
            "classpath:database/cars/insert_one_car.sql",
            "classpath:database/rentals/insert_one_reserved_rental.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void returnRental_TooLateToCancel_BadRequest() throws Exception {
        //Given
        Long rentalId = 1L;

        //When & Then
        mockMvc.perform(
                        post("/rentals/{rentalId}/returns", rentalId))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("""
            returnRental():
             Should return 401 UNAUTHORIZED when user doesn't authorized
            """)
    void returnRental_WithoutAuth_Unauthorized() throws Exception {
        //Given
        Long rentalId = 1L;
        //When & Then
        mockMvc.perform(post("/rentals/{rentalId}/returns", rentalId))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
