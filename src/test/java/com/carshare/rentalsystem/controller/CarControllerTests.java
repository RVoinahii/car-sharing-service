package com.carshare.rentalsystem.controller;

import static com.carshare.rentalsystem.test.util.TestCarDataUtil.CAR_NEW_BRAND;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.CAR_NEW_MODEL;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_SIZE;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createCarRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createDefaultCarDtoSample;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createDefaultCarPreviewDtoSample;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createUpdateCarRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.CUSTOMER_AUTHORITY;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.MANAGER_AUTHORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carshare.rentalsystem.dto.car.request.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.request.dto.InventoryUpdateRequestDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarResponseDto;
import com.carshare.rentalsystem.service.car.CarService;
import com.carshare.rentalsystem.test.util.TestPageImplDeserializerUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTests {

    protected static MockMvc mockMvc;

    @Autowired
    private CarService bookService;

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
            getAll():
             Should return all books when valid pagination is provided
              and no search parameters are set
            """)
    @Sql(scripts = "classpath:database/cars/insert_three_cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_ValidPageableWithoutParameters_Success() throws Exception {
        //Given
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new TestPageImplDeserializerUtil<>(
                CarPreviewResponseDto.class, PAGE_SIZE));
        localMapper.registerModule(module);

        //When
        MvcResult result = mockMvc.perform(get("/cars")
                        .param("model", "")
                        .param("brand", "")
                        .param("type", "")
                        .param("priceRange", "")
                        .param("onlyAvailable", "")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        PageImpl<CarPreviewResponseDto> actualCarDtosPage =
                localMapper.readValue(result.getResponse()
                .getContentAsString(), new TypeReference<>() {});

        assertNotNull(actualCarDtosPage);
        assertEquals(3, actualCarDtosPage.getTotalElements());
        assertEquals(PAGE_SIZE, actualCarDtosPage.getSize());
    }

    @Test
    @DisplayName("""
            getAll():
             Should return cars matching the search when valid pagination
              and search parameters are provided
            """)
    @Sql(scripts = "classpath:database/cars/insert_three_cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_ValidPageableWithParameter_Success() throws Exception {
        //Given
        Long expectedId = 3L;
        String expectedModel = "CarModelThree";
        String expectedBrand = "CarBrandThree";
        String expectedType = "UNIVERSAL";
        BigDecimal expectedDailyFee = new BigDecimal("59.99");

        CarPreviewResponseDto expectedCarDto = createDefaultCarPreviewDtoSample();
        expectedCarDto.setId(expectedId);
        expectedCarDto.setModel(expectedModel);
        expectedCarDto.setBrand(expectedBrand);
        expectedCarDto.setType(expectedType);
        expectedCarDto.setDailyFee(expectedDailyFee);

        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new TestPageImplDeserializerUtil<>(
                CarPreviewResponseDto.class, PAGE_SIZE));
        localMapper.registerModule(module);

        //When
        MvcResult result = mockMvc.perform(get("/cars")
                        .param("type", expectedType)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        PageImpl<CarPreviewResponseDto> actualCarDtosPage =
                localMapper.readValue(result.getResponse()
                .getContentAsString(), new TypeReference<>() {});

        assertNotNull(actualCarDtosPage);
        assertEquals(1, actualCarDtosPage.getTotalElements());
        assertEquals(PAGE_SIZE, actualCarDtosPage.getSize());
        assertTrue(EqualsBuilder.reflectionEquals(
                actualCarDtosPage.getContent().getFirst(), expectedCarDto));
    }

    @Test
    @DisplayName("""
            getCarById():
             Verifying retrieval of a car by its ID
            """)
    @Sql(scripts = "classpath:database/cars/insert_one_car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCarById_ValidId_Success() throws Exception {
        //Given
        Long carId = 1L;

        CarResponseDto expectedCarDto = createDefaultCarDtoSample();
        expectedCarDto.setId(carId);
        //When
        MvcResult result = mockMvc.perform(get("/cars/{carId}", carId))
                .andExpect(status().isOk())
                .andReturn();
        //Then
        CarResponseDto actualCarDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarResponseDto.class);
        assertNotNull(actualCarDto);
        assertTrue(EqualsBuilder.reflectionEquals(actualCarDto, expectedCarDto));
    }

    @Test
    @DisplayName("""
            getCarById():
             Should return 404 NOT FOUND when given invalid ID
            """)
    void getCarById_InvalidId_NotFound() throws Exception {
        //Given
        Long carId = 99L;
        //When & Then
        mockMvc.perform(get("/cars/{carId}", carId))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            createCar():
             Confirming successful creation of a car with valid request
            """)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createCar_ValidRequestDto_Created() throws Exception {
        //Given
        CreateCarRequestDto requestDto = createCarRequestDtoSample();
        CarResponseDto expectedCarDto = createDefaultCarDtoSample();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        CarResponseDto actualCarDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarResponseDto.class);
        assertNotNull(actualCarDto);
        assertNotNull(actualCarDto.getId());
        assertTrue(EqualsBuilder.reflectionEquals(actualCarDto, expectedCarDto, "id"));
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            createCar():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    void createCar_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        CreateCarRequestDto requestDto = new CreateCarRequestDto(
                null, null, null, 0, null);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "customer", authorities = CUSTOMER_AUTHORITY)
    @Test
    @DisplayName("""
        createCar():
         Should return 403 FORBIDDEN when user doesn't have authority 'MANAGER'
            """)
    void createBook_UserWithoutRequiredAuthority_Forbidden() throws Exception {
        //Given
        CreateCarRequestDto requestDto = createCarRequestDtoSample();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateCarById():
             Verifying updating car data by ID with valid request
            """)
    @Sql(scripts = "classpath:database/cars/insert_one_car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCarById_ValidRequestDtoAndId_Success() throws Exception {
        //Given
        CreateCarRequestDto requestDto = createUpdateCarRequestDtoSample();
        Long carId = 1L;

        CarResponseDto expectedCarDto = createDefaultCarDtoSample();
        expectedCarDto.setId(carId);
        expectedCarDto.setModel(CAR_NEW_MODEL);
        expectedCarDto.setBrand(CAR_NEW_BRAND);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        patch("/cars/{carId}", carId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CarResponseDto actualCarDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarResponseDto.class);
        assertNotNull(actualCarDto);
        assertNotNull(actualCarDto.getId());
        assertTrue(EqualsBuilder.reflectionEquals(actualCarDto, expectedCarDto));
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
        updateCarById():
         Should return 404 NOT FOUND when given invalid ID
            """)
    void updateCarByById_InvalidId_NotFound() throws Exception {
        //Given
        Long carId = 99L;

        CreateCarRequestDto requestDto = createCarRequestDtoSample();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(patch("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateCarById():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    void updateCarById_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        Long carId = 1L;

        CreateCarRequestDto requestDto = new CreateCarRequestDto(
                null, null, null, 0, null);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(patch("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "customer", authorities = CUSTOMER_AUTHORITY)
    @Test
    @DisplayName("""
        updateCarById():
         Should return 403 FORBIDDEN when user doesn't have authority 'MANAGER'
            """)
    void updateCarById_UserWithoutRequiredAuthority_Forbidden() throws Exception {
        //Given
        Long carId = 1L;

        CreateCarRequestDto requestDto = createCarRequestDtoSample();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(patch("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateCarInventoryById():
             Verifying updating car inventory data by ID with valid request
            """)
    @Sql(scripts = "classpath:database/cars/insert_one_car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCarInventoryById_ValidRequestDtoAndId_Success() throws Exception {
        //Given
        InventoryUpdateRequestDto requestDto = new InventoryUpdateRequestDto(30);
        Long carId = 1L;

        CarResponseDto expectedCarDto = createDefaultCarDtoSample();
        expectedCarDto.setId(carId);
        expectedCarDto.setInventory(requestDto.inventory());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        put("/cars/{carId}", carId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CarResponseDto actualCarDto = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarResponseDto.class);
        assertNotNull(actualCarDto);
        assertNotNull(actualCarDto.getId());
        assertTrue(EqualsBuilder.reflectionEquals(actualCarDto, expectedCarDto));
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateCarInventoryById():
             Should return 404 NOT FOUND when given invalid ID
            """)
    void updateCarInventoryById_InvalidId_NotFound() throws Exception {
        //Given
        Long carId = 1L;

        InventoryUpdateRequestDto requestDto = new InventoryUpdateRequestDto(30);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(put("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            updateCarInventoryById():
             Should return 400 BAD REQUEST when given invalid request body
            """)
    void updateCarInventoryById_InvalidRequestDto_BadRequest() throws Exception {
        //Given
        Long carId = 1L;

        InventoryUpdateRequestDto requestDto = new InventoryUpdateRequestDto(-1);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(put("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "customer", authorities = CUSTOMER_AUTHORITY)
    @Test
    @DisplayName("""
        updateCarInventoryById():
         Should return 403 FORBIDDEN when user doesn't have authority 'MANAGER'
            """)
    void updateCarInventoryById_UserWithoutRequiredAuthority_Forbidden() throws Exception {
        //Given
        Long carId = 1L;

        InventoryUpdateRequestDto requestDto = new InventoryUpdateRequestDto(30);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When & Then
        mockMvc.perform(put("/cars/{carId}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "manager", authorities = MANAGER_AUTHORITY)
    @Test
    @DisplayName("""
            deleteCar():
             Verifying successful car removal by its ID
            """)
    @Sql(scripts = "classpath:database/cars/insert_one_car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/clear_database.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteCar_ValidId_NoContent() throws Exception {
        //Given
        Long carId = 1L;

        //When
        MvcResult result = mockMvc.perform(delete("/cars/{carId}", carId))
                .andExpect(status().isNoContent())
                .andReturn();
        //Then
        mockMvc.perform(get("/cars/{carId}", carId)).andExpect(status().isNotFound());
    }

    @WithMockUser(username = "customer", authorities = CUSTOMER_AUTHORITY)
    @Test
    @DisplayName("""
        deleteCar():
         Should return 403 FORBIDDEN when user doesn't have authority 'MANAGER'
            """)
    void deleteCar_UserWithoutRequiredAuthority_Forbidden() throws Exception {
        //Given
        Long carId = 1L;

        //When & Then
        mockMvc.perform(delete("/cars/{carId}", carId))
                .andExpect(status().isForbidden());
    }
}
