package com.carshare.rentalsystem.service.car;

import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_NUMBER;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_SIZE;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createCarDtoSampleFromEntity;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createCarPreviewDtoSampleFromEntity;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createCarRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createCarSampleFromRequest;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createDefaultCarSample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.dto.car.request.dto.CarSearchParameters;
import com.carshare.rentalsystem.dto.car.request.dto.CreateCarRequestDto;
import com.carshare.rentalsystem.dto.car.request.dto.InventoryUpdateRequestDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.car.response.dto.CarResponseDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.mapper.CarMapper;
import com.carshare.rentalsystem.mapper.search.CarSearchParametersMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.repository.SpecificationBuilder;
import com.carshare.rentalsystem.repository.SpecificationBuilderFactory;
import com.carshare.rentalsystem.repository.car.CarRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class CarServiceTests {
    @InjectMocks
    private CarServiceImpl carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private CarSearchParametersMapper searchParametersMapper;

    @Mock
    private SpecificationBuilderFactory specificationBuilderFactory;

    @Mock
    private SpecificationBuilder<Car, Map<String, String>> specificationBuilder;

    @Test
    @DisplayName("""
            getAll():
             Should return correct Page<CarPreviewDto> when pageable
              is valid and search parameters is not provided
            """)
    void getAll_ValidPageableWithoutSearchParam_ReturnsAllCars() {
        //Given
        CarSearchParameters searchParameters = new CarSearchParameters(
                null, null, null, null, null
        );
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<Car> specification = mock(Specification.class);
        when(specificationBuilderFactory.getBuilder(Car.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);

        Car car = createDefaultCarSample();
        CarPreviewResponseDto expectedCarDto = createCarPreviewDtoSampleFromEntity(car);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Car> cars = List.of(
                createDefaultCarSample(),
                createDefaultCarSample(),
                createDefaultCarSample()
        );
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        when(specificationBuilderFactory.getBuilder(Car.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);
        when(carRepository.findAll(eq(specification), eq(pageable))).thenReturn(carPage);
        when(carMapper.toPreviewDto(any(Car.class))).thenReturn(expectedCarDto);

        //When
        Page<CarPreviewResponseDto> actualCarPreviewDtosPage =
                carService.getAll(searchParameters, pageable);

        //Then
        assertThat(actualCarPreviewDtosPage).hasSize(3);
        verify(carRepository).findAll(specification, pageable);
        verify(carMapper, times(3)).toPreviewDto(any(Car.class));
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
            getAll():
             Should return correct Page<CarPreviewDto> when pageable
              is valid and search parameter is provided
            """)
    void getAll_ValidPageableWithSearchParam_ReturnsSpecificCars() {
        //Given
        CarSearchParameters searchParameters = new CarSearchParameters(
                "Tesla", null, null, null, null
        );
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<Car> specification = mock(Specification.class);
        when(specificationBuilderFactory.getBuilder(Car.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);

        Car car = createDefaultCarSample();
        car.setModel("Tesla");
        CarPreviewResponseDto expectedCarDto = createCarPreviewDtoSampleFromEntity(car);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Car> cars = List.of(car);
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        when(specificationBuilderFactory.getBuilder(Car.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);
        when(carRepository.findAll(eq(specification), eq(pageable))).thenReturn(carPage);
        when(carMapper.toPreviewDto(car)).thenReturn(expectedCarDto);

        //When
        Page<CarPreviewResponseDto> actualCarPreviewDtosPage =
                carService.getAll(searchParameters, pageable);

        //Then
        assertThat(actualCarPreviewDtosPage).hasSize(1);
        assertThat(actualCarPreviewDtosPage.getContent().getFirst()).isEqualTo(expectedCarDto);
        verify(carRepository).findAll(specification, pageable);
        verify(carMapper).toPreviewDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
            getById():
             Should return correct CarResponseDto when car exists
            """)
    void getById_WithValidCarId_ShouldReturnValidCarDto() {
        //Given
        Long carId = 1L;

        Car car = createDefaultCarSample();
        CarResponseDto expectedCarDto = createCarDtoSampleFromEntity(car);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedCarDto);

        //When
        CarResponseDto actualCarDto = carService.getById(carId);

        //Then
        assertThat(actualCarDto).isEqualTo(expectedCarDto);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
            getById():
             Should return correct CarResponseDto when car exists
            """)
    void getById_WithInvalidCarId_ShouldThrowException() {
        //Given
        Long carId = 999L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        //When
        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> carService.getById(carId)
        );

        //Then
        String expected = "Can't find car by id: " + carId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("""
            create():
             Should return the correct CarResponseDto when a car is successfully created
            """)
    void create_ValidCreateBookRequestDto_ReturnsBookDto() {
        //Given
        CreateCarRequestDto requestDto = createCarRequestDtoSample();
        Car car = createCarSampleFromRequest(requestDto);
        CarResponseDto expectedCarDto = createCarDtoSampleFromEntity(car);

        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expectedCarDto);

        //When
        CarResponseDto actualCarDto = carService.create(requestDto);

        //Then
        assertThat(actualCarDto).isEqualTo(expectedCarDto);
        verify(carRepository).save(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
            updateCarById():
             Should return correct CarResponseDto when car is successfully updated
            """)
    void updateCarById_WithValidId_ShouldReturnValidDto() {
        //Given
        Car existingCar = createDefaultCarSample();
        existingCar.setModel("OldModel");
        existingCar.setBrand("OldBrand");

        Car updatedCar = createDefaultCarSample();

        CarResponseDto expectedCarDto = createCarDtoSampleFromEntity(updatedCar);

        Long carId = 1L;
        CreateCarRequestDto requestDto = createCarRequestDtoSample();

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carMapper.toDto(carRepository.save(updatedCar))).thenReturn(expectedCarDto);

        // When
        CarResponseDto actualCarDto = carService.updateCarById(carId, requestDto);

        // Then
        assertThat(actualCarDto).isEqualTo(expectedCarDto);
        verify(carRepository).findById(carId);
        verify(carRepository).save(updatedCar);
        verify(carMapper).toDto(carRepository.save(updatedCar));
    }

    @Test
    @DisplayName("""
            updateCarById():
             Should throw EntityNotFoundException when the car doesn't exist during update
            """)
    void updateCarById_WithInvalidId_ShouldThrowException() {
        //Given
        CreateCarRequestDto requestDto = createCarRequestDtoSample();

        Long carId = 1L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                    EntityNotFoundException.class, () -> carService.updateCarById(carId,
                        requestDto)
        );

        // Then
        String expected = "Can't find car by id: " + carId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("""
            updateInventoryByCarId():
             Should return correct CarResponseDto when car inventory is successfully updated
            """)
    void updateInventoryByCarId_WithValidId_ShouldReturnValidDto() {
        //Given
        InventoryUpdateRequestDto requestDto = new InventoryUpdateRequestDto(20);

        Car existingCar = createDefaultCarSample();

        Car updatedCar = createDefaultCarSample();
        updatedCar.setInventory(requestDto.inventory());

        CarResponseDto expectedCarDto = createCarDtoSampleFromEntity(updatedCar);

        Long carId = 1L;

        when(carRepository.findById(carId)).thenReturn(Optional.of(existingCar));
        when(carMapper.toDto(carRepository.save(updatedCar))).thenReturn(expectedCarDto);

        // When
        CarResponseDto actualCarDto = carService.updateInventoryByCarId(carId, requestDto);

        // Then
        assertThat(actualCarDto).isEqualTo(expectedCarDto);
        verify(carRepository).findById(carId);
        verify(carRepository).save(updatedCar);
        verify(carMapper).toDto(carRepository.save(updatedCar));
    }

    @Test
    @DisplayName("""
            updateInventoryByCarId():
             Should throw EntityNotFoundException when the car doesn't exist during inventory update
            """)
    void updateInventoryByCarId_WithInvalidId_ShouldThrowException() {
        //Given
        InventoryUpdateRequestDto requestDto = new InventoryUpdateRequestDto(20);

        Long carId = 999L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> carService.updateInventoryByCarId(carId,
                        requestDto)
        );

        // Then
        String expected = "Can't find car by id: " + carId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(carRepository).findById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("""
            deleteById():
             Should delete car by ID when a valid ID is provided
            """)
    void deleteById_WithValidId_ShouldInvokeRepositoryOnce() {
        //Given
        Long carId = 1L;

        //When
        carService.deleteById(carId);

        //Then
        verify(carRepository).deleteById(carId);
        verifyNoMoreInteractions(carRepository);
    }
}
