package com.carshare.rentalsystem.service.rental;

import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_NUMBER;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.PAGE_SIZE;
import static com.carshare.rentalsystem.test.util.TestCarDataUtil.createDefaultCarSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createActiveRentalSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createCompletedRentalSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createRentalDtoSampleFromEntity;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createRentalFromRequest;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createRentalPreviewDtoSampleFromEntity;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createRentalRequestDtoSample;
import static com.carshare.rentalsystem.test.util.TestRentalDataUtil.createReservedRentalSample;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.DEFAULT_ID_SAMPLE;
import static com.carshare.rentalsystem.test.util.TestUserDataUtil.createDefaultUserSample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.carshare.rentalsystem.dto.rental.event.dto.RentalCreatedEventDto;
import com.carshare.rentalsystem.dto.rental.event.dto.RentalReturnEventDto;
import com.carshare.rentalsystem.dto.rental.request.dto.CreateRentalRequestDto;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.exception.CarNotAvailableException;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.exception.MaxActiveRentalsExceededException;
import com.carshare.rentalsystem.exception.RentalAccessDeniedException;
import com.carshare.rentalsystem.exception.RentalAlreadyReturnedException;
import com.carshare.rentalsystem.exception.TooLateToCancelRentalException;
import com.carshare.rentalsystem.mapper.RentalMapper;
import com.carshare.rentalsystem.mapper.search.RentalSearchParametersMapper;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.repository.SpecificationBuilder;
import com.carshare.rentalsystem.repository.SpecificationBuilderFactory;
import com.carshare.rentalsystem.repository.car.CarRepository;
import com.carshare.rentalsystem.repository.rental.RentalRepository;
import com.carshare.rentalsystem.repository.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTests {
    @InjectMocks
    private RentalServiceImpl rentalService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private RentalSearchParametersMapper searchParametersMapper;

    @Mock
    private SpecificationBuilderFactory specificationBuilderFactory;

    @Mock
    private SpecificationBuilder<Rental, Map<String, String>> specificationBuilder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    @DisplayName("""
    create():
     Should create ACTIVE rental and return RentalResponseDto
     when user has less than 3 active rentals and rentalDate is today
            """)
    void create_UserHasLessThanMaxActiveRentalsAndRentalDateIsToday_ShouldCreateActiveRental() {
        //Given
        Long userId = 1L;
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        Rental rental = createRentalFromRequest(requestDto);
        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);

        Car car = createDefaultCarSample();
        User user = createDefaultUserSample();

        when(rentalRepository.countUserActiveRentals(eq(userId), anyList())).thenReturn(1L);
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(rentalMapper.toEntity(requestDto)).thenReturn(rental);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        //When
        RentalResponseDto actualResponseDto = rentalService.create(userId, requestDto);

        //Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);
        verify(rentalRepository).countUserActiveRentals(eq(userId), anyList());
        verify(carRepository).findById(requestDto.getCarId());
        verify(carRepository).save(any(Car.class));
        verify(rentalMapper).toEntity(requestDto);
        verify(userRepository).findById(userId);
        verify(rentalRepository).save(rental);
        verify(rentalMapper).toDto(rental);
        verify(applicationEventPublisher).publishEvent(any(RentalCreatedEventDto.class));
        verifyNoMoreInteractions(rentalRepository, carRepository,
                rentalMapper, userRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    create():
     Should create RESERVED rental and return RentalResponseDto
     when user has less than 3 active rentals and rentalDate is in the future
            """)
    void create_UserHasLessThanMaxActiveRentalsAndRentalDateIsToday_ShouldCreateReservedRental() {
        // Given
        Long userId = 1L;
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        requestDto.setRentalDate(LocalDate.now().plusDays(3));

        Rental rental = createRentalFromRequest(requestDto);
        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);

        Car car = createDefaultCarSample();
        User user = createDefaultUserSample();

        when(rentalRepository.countUserActiveRentals(eq(userId), anyList())).thenReturn(0L);
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(rentalMapper.toEntity(requestDto)).thenReturn(rental);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        // When
        RentalResponseDto actualResponseDto = rentalService.create(userId, requestDto);

        // Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);
        assertThat(rental.getStatus()).isEqualTo(Rental.RentalStatus.RESERVED);

        verify(rentalRepository).countUserActiveRentals(eq(userId), anyList());
        verify(carRepository).findById(requestDto.getCarId());
        verify(carRepository).save(any(Car.class));
        verify(rentalMapper).toEntity(requestDto);
        verify(userRepository).findById(userId);
        verify(rentalRepository).save(rental);
        verify(rentalMapper).toDto(rental);
        verify(applicationEventPublisher).publishEvent(any(RentalCreatedEventDto.class));
        verifyNoMoreInteractions(rentalRepository, carRepository,
                rentalMapper, userRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    create():
     Should throw MaxActiveRentalsExceededException when user has 3 or more active rentals
            """)
    void create_UserHasMaxActiveRentals_ShouldThrowException() {
        //Given
        Long userId = 1L;
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();

        when(rentalRepository.countUserActiveRentals(eq(userId), anyList())).thenReturn(3L);

        //When & Then
        MaxActiveRentalsExceededException ex = assertThrows(
                MaxActiveRentalsExceededException.class,
                () -> rentalService.create(userId, requestDto)
        );

        assertThat(ex.getMessage())
                .contains("User already has 3 active rentals. Maximum allowed: 3");

        verify(rentalRepository).countUserActiveRentals(eq(userId), anyList());
        verifyNoMoreInteractions(rentalRepository, carRepository,
                rentalMapper, userRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    create():
     Should throw EntityNotFoundException when user is not found
            """)
    void create_UserNotFound_ShouldThrowException() {
        //Given
        Long userId = DEFAULT_ID_SAMPLE;
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        Car car = createDefaultCarSample();
        Rental rental = createRentalFromRequest(requestDto);

        when(rentalRepository.countUserActiveRentals(eq(userId), anyList())).thenReturn(0L);
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(rentalMapper.toEntity(requestDto)).thenReturn(rental);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //When & Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.create(userId, requestDto)
        );

        assertThat(ex.getMessage())
                .contains("Can't find user with id: " + userId);

        verify(rentalRepository).countUserActiveRentals(eq(userId), anyList());
        verify(carRepository).findById(requestDto.getCarId());
        verify(carRepository).save(any(Car.class));
        verify(rentalMapper).toEntity(requestDto);
        verify(userRepository).findById(userId);

        verifyNoMoreInteractions(rentalRepository, carRepository,
                rentalMapper, userRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    create():
     Should throw EntityNotFoundException when car is not found
            """)
    void create_CarNotFound_ShouldThrowException() {
        // Given
        Long userId = DEFAULT_ID_SAMPLE;
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        User user = createDefaultUserSample();

        when(rentalRepository.countUserActiveRentals(eq(userId), anyList())).thenReturn(0L);
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.create(userId, requestDto)
        );

        assertThat(ex.getMessage()).contains("Can't find car with id: " + requestDto.getCarId());

        verify(rentalRepository).countUserActiveRentals(eq(userId), anyList());
        verify(carRepository).findById(requestDto.getCarId());

        verifyNoMoreInteractions(rentalRepository, carRepository,
                rentalMapper, userRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    create():
     Should throw CarNotAvailableException when car inventory is insufficient
            """)
    void create_CarInventoryTooLow_ShouldThrowException() {
        // Given
        Long userId = DEFAULT_ID_SAMPLE;
        CreateRentalRequestDto requestDto = createRentalRequestDtoSample();
        Car car = createDefaultCarSample();
        car.setInventory(0); // недостатньо машин

        when(rentalRepository.countUserActiveRentals(eq(userId), anyList())).thenReturn(0L);
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));

        // When & Then
        CarNotAvailableException ex = assertThrows(
                CarNotAvailableException.class,
                () -> rentalService.create(userId, requestDto)
        );

        assertThat(ex.getMessage()).contains("Car with id: " + car.getId() + " is out of stock");

        verify(rentalRepository).countUserActiveRentals(eq(userId), anyList());
        verify(carRepository).findById(requestDto.getCarId());

        verifyNoMoreInteractions(rentalRepository, carRepository,
                rentalMapper, userRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
            getRentalsById():
             Should return Page<RentalPreviewResponseDto> for the given userId and pageable
            """)
    void getRentalsById_ValidPageable_ShouldReturnAllRentals() {
        //Given
        Long userId = 1L;

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Rental rental = createCompletedRentalSample();
        RentalPreviewResponseDto expectedRentalDto =
                createRentalPreviewDtoSampleFromEntity(rental);

        List<Rental> rentals = List.of(rental);
        Page<Rental> rentalPage = new PageImpl<>(rentals, pageable, rentals.size());

        when(rentalRepository.findAllByUserIdWithCarAndUser(userId, pageable))
                .thenReturn(rentalPage);
        when(rentalMapper.toPreviewDto(rental)).thenReturn(expectedRentalDto);

        //When
        Page<RentalPreviewResponseDto> actucalOrderDtosPage =
                rentalService.getRentalsById(userId, pageable);

        //Then
        assertThat(actucalOrderDtosPage).hasSize(1);
        assertThat(actucalOrderDtosPage.getContent().getFirst()).isEqualTo(expectedRentalDto);
        verify(rentalRepository).findAllByUserIdWithCarAndUser(userId, pageable);
        verify(rentalMapper).toPreviewDto(rental);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
            getSpecificRentals():
             Should return correct Page<RentalPreviewResponseDto> when pageable
              is valid and search parameters is not provided
            """)
    void getSpecificRentals_ValidPageableWithoutSearchParam_ReturnsAllCars() {
        //Given
        RentalSearchParameters searchParameters = new RentalSearchParameters(null, null);
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<Rental> specification = mock(Specification.class);
        when(specificationBuilderFactory.getBuilder(Rental.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);

        Rental rental = createCompletedRentalSample();
        RentalPreviewResponseDto expectedRentalPreviewDto =
                createRentalPreviewDtoSampleFromEntity(rental);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Rental> rentals = List.of(
                createCompletedRentalSample(),
                createCompletedRentalSample(),
                createCompletedRentalSample()
        );
        Page<Rental> rentalPage = new PageImpl<>(rentals, pageable, rentals.size());

        when(specificationBuilderFactory.getBuilder(Rental.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);
        when(rentalRepository.findAll(eq(specification), eq(pageable))).thenReturn(rentalPage);
        when(rentalMapper.toPreviewDto(any(Rental.class))).thenReturn(expectedRentalPreviewDto);

        //When
        Page<RentalPreviewResponseDto> actualRentalPreviewDtosPage =
                rentalService.getSpecificRentals(searchParameters, pageable);

        //Then
        assertThat(actualRentalPreviewDtosPage).hasSize(3);
        verify(rentalRepository).findAll(specification, pageable);
        verify(rentalMapper, times(3)).toPreviewDto(any(Rental.class));
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
            getSpecificRentals():
             Should return correct Page<RentalPreviewResponseDto> when pageable
              is valid and search parameter is provided
            """)
    void getSpecificRentals_ValidPageableWithSearchParam_ReturnsSpecificRentals() {
        //Given
        RentalSearchParameters searchParameters = new RentalSearchParameters(
                null, Rental.RentalStatus.WAITING_FOR_PAYMENT);
        Map<String, String> filters = searchParametersMapper.toMap(searchParameters);

        Specification<Rental> specification = mock(Specification.class);
        when(specificationBuilderFactory.getBuilder(Rental.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);

        Rental rental = createCompletedRentalSample();
        rental.setStatus(Rental.RentalStatus.WAITING_FOR_PAYMENT);
        RentalPreviewResponseDto expectedRentalPreviewDto =
                createRentalPreviewDtoSampleFromEntity(rental);

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        List<Rental> rentals = List.of(rental);
        Page<Rental> rentalPage = new PageImpl<>(rentals, pageable, rentals.size());

        when(specificationBuilderFactory.getBuilder(Rental.class)).thenReturn(specificationBuilder);
        when(specificationBuilder.build(filters)).thenReturn(specification);
        when(rentalRepository.findAll(eq(specification), eq(pageable))).thenReturn(rentalPage);
        when(rentalMapper.toPreviewDto(rental)).thenReturn(expectedRentalPreviewDto);

        //When
        Page<RentalPreviewResponseDto> actualRentalPreviewDtosPage =
                rentalService.getSpecificRentals(searchParameters, pageable);

        //Then
        assertThat(actualRentalPreviewDtosPage).hasSize(1);
        assertThat(actualRentalPreviewDtosPage.getContent().getFirst())
                .isEqualTo(expectedRentalPreviewDto);
        verify(rentalRepository).findAll(specification, pageable);
        verify(rentalMapper).toPreviewDto(rental);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
    getAnyRentalInfo():
     Should return RentalResponseDto when rental exists
            """)
    void getAnyRentalInfo_RentalExists_ShouldReturnDto() {
        //Given
        Long rentalId = 1L;
        Rental rental = createCompletedRentalSample();
        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        //When
        RentalResponseDto actualResponseDto = rentalService.getAnyRentalInfo(rentalId);

        //Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verify(rentalMapper).toDto(rental);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
    getAnyRentalInfo():
     Should throw EntityNotFoundException when rental is not found
            """)
    void getAnyRentalInfo_RentalNotFound_ShouldThrowException() {
        //Given
        Long invalidRentalId = 999L;

        when(rentalRepository.findByIdWithCarAndUser(invalidRentalId))
                .thenReturn(Optional.empty());

        //When & Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.getAnyRentalInfo(invalidRentalId)
        );

        assertThat(ex.getMessage()).contains("Can't find rental with id: " + invalidRentalId);

        verify(rentalRepository).findByIdWithCarAndUser(invalidRentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
    getCustomerRentalInfo():
     Should return RentalResponseDto when rental belongs to user
            """)
    void getCustomerRentalInfo_RentalBelongsToUser_ShouldReturnDto() {
        //Given
        Long rentalId = 1L;
        Long userId = 1L;
        Rental rental = createCompletedRentalSample();
        rental.getUser().setId(userId);

        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        //When
        RentalResponseDto actualResponseDto = rentalService
                .getCustomerRentalInfo(userId, rentalId);

        //Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verify(rentalMapper).toDto(rental);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
    getCustomerRentalInfo():
     Should throw EntityNotFoundException when rental is not found
            """)
    void getCustomerRentalInfo_RentalNotFound_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Long invalidRentalId = 999L;

        when(rentalRepository.findByIdWithCarAndUser(invalidRentalId))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.getCustomerRentalInfo(userId, invalidRentalId)
        );

        assertThat(ex.getMessage()).contains("Can't find rental with id: " + invalidRentalId);

        verify(rentalRepository).findByIdWithCarAndUser(invalidRentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
    getCustomerRentalInfo():
     Should throw EntityNotFoundException when rental does not belong to user
            """)
    void getCustomerRentalInfo_RentalDoesNotBelongToUser_ShouldThrowException() {
        //Given
        Long rentalId = 1L;
        Long userId = 1L;
        Long anotherUserId = 999L;

        Rental rental = createCompletedRentalSample();
        rental.getUser().setId(anotherUserId);

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));

        //When & Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.getCustomerRentalInfo(userId, rentalId)
        );

        assertThat(ex.getMessage()).contains("Can't find rental with id: " + rentalId);

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should return RentalResponseDto with WAITING_FOR_PAYMENT status
     when rental is returned successfully
            """)
    void returnRental_ValidActiveRental_ShouldReturnWaitingFroPaymentRentalResponseDto() {
        //Given
        Rental rental = createActiveRentalSample();

        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);
        expectedResponseDto.setActualReturnDate(LocalDate.now());
        expectedResponseDto.setStatus(Rental.RentalStatus.WAITING_FOR_PAYMENT.name());

        Long rentalId = 1L;
        Long userId = 1L;

        Car car = createDefaultCarSample();

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(carRepository.findById(rental.getCar().getId())).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        //When
        RentalResponseDto actualResponseDto = rentalService.returnRental(userId, rentalId);

        //Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verify(rentalRepository).save(rental);
        verify(carRepository).findById(rental.getCar().getId());
        verify(carRepository).save(any(Car.class));
        verify(rentalMapper).toDto(rental);
        verify(applicationEventPublisher).publishEvent(any(RentalReturnEventDto.class));
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should CANCEL RESERVED rental when cancelling in advance (>= MAX_EARLY_CANCEL_DAYS)
            """)
    void returnRental_ValidReservedRental_CancelledWhenInAdvance() {
        // Given
        Rental rental = createReservedRentalSample();

        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);
        expectedResponseDto.setActualReturnDate(LocalDate.now());
        expectedResponseDto.setStatus(Rental.RentalStatus.CANCELLED.name());

        Long rentalId = 1L;
        Long userId = 1L;

        Car car = createDefaultCarSample();

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(carRepository.findById(rental.getCar().getId())).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        // When
        RentalResponseDto actualResponseDto = rentalService.returnRental(userId, rentalId);

        // Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);
        assertThat(rental.getStatus()).isEqualTo(Rental.RentalStatus.CANCELLED);
        assertThat(rental.getActualReturnDate()).isEqualTo(LocalDate.now());

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verify(rentalRepository).save(rental);
        verify(carRepository).findById(rental.getCar().getId());
        verify(carRepository).save(any(Car.class));
        verify(rentalMapper).toDto(rental);
        verify(applicationEventPublisher).publishEvent(any(RentalReturnEventDto.class));
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should mark RESERVED rental as WAITING_FOR_PAYMENT
      when cancelling after FREE_CANCELLATION_THRESHOLD
            """)
    void returnRental_ValidReservedRental_CancellationIsPaid() {
        //Given
        Rental rental = createReservedRentalSample();
        rental.setReturnDate(rental.getReturnDate().plusDays(3));

        RentalResponseDto expectedResponseDto = createRentalDtoSampleFromEntity(rental);
        expectedResponseDto.setActualReturnDate(LocalDate.now());
        expectedResponseDto.setStatus(Rental.RentalStatus.WAITING_FOR_PAYMENT.name());

        Long rentalId = 1L;
        Long userId = 1L;

        Car car = createDefaultCarSample();

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(carRepository.findById(rental.getCar().getId())).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalMapper.toDto(rental)).thenReturn(expectedResponseDto);

        //When
        RentalResponseDto actualResponseDto = rentalService.returnRental(userId, rentalId);

        //Then
        assertThat(actualResponseDto).isEqualTo(expectedResponseDto);
        assertThat(rental.getStatus()).isEqualTo(Rental.RentalStatus.WAITING_FOR_PAYMENT);
        assertThat(rental.getActualReturnDate()).isEqualTo(LocalDate.now());

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verify(rentalRepository).save(rental);
        verify(carRepository).findById(rental.getCar().getId());
        verify(carRepository).save(any(Car.class));
        verify(rentalMapper).toDto(rental);
        verify(applicationEventPublisher).publishEvent(any(RentalReturnEventDto.class));
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should throw EntityNotFoundException when rental is not found
            """)
    void returnRental_RentalNotFound_ShouldThrowException() {
        //Given
        Long userId = 1L;
        Long invalidRentalId = 999L;

        when(rentalRepository.findByIdWithCarAndUser(invalidRentalId))
                .thenReturn(Optional.empty());

        //When & Then
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.returnRental(userId, invalidRentalId)
        );

        assertThat(ex.getMessage()).contains("Can't find rental with id: " + invalidRentalId);

        verify(rentalRepository).findByIdWithCarAndUser(invalidRentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should throw RentalAccessDeniedException when rental belongs to another user
            """)
    void returnRental_RentalBelongsToAnotherUser_ShouldThrowException() {
        //Given
        Long userId = 1L;
        Long rentalId = 1L;
        Long anotherUserId = 999L;

        Rental rental = createCompletedRentalSample();
        rental.getUser().setId(anotherUserId);

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));

        //When & Then
        RentalAccessDeniedException ex = assertThrows(
                RentalAccessDeniedException.class,
                () -> rentalService.returnRental(userId, rentalId)
        );

        assertThat(ex.getMessage()).contains("You do not have permission to return this rental");

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should throw TooLateToCancelRentalException when trying to cancel RESERVED rental too late
            """)
    void returnRental_TooLateToCancelReserved_ShouldThrowException() {
        //Given
        Rental rental = createActiveRentalSample();
        rental.setStatus(Rental.RentalStatus.RESERVED);
        rental.setRentalDate(LocalDate.now().plusDays(1 - 5));

        Long rentalId = 1L;
        Long userId = 1L;

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));

        //When & Then
        TooLateToCancelRentalException ex = assertThrows(
                TooLateToCancelRentalException.class,
                () -> rentalService.returnRental(userId, rentalId)
        );

        assertThat(ex.getMessage())
                .contains("You can only cancel a reserved rental at least");

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }

    @Test
    @DisplayName("""
    returnRental():
     Should throw RentalAlreadyReturnedException when rental is already returned
            """)
    void returnRental_RentalAlreadyReturned_ShouldThrowException() {
        //Given
        Long userId = 1L;
        Long rentalId = 1L;

        Rental rental = createActiveRentalSample();
        rental.getUser().setId(userId);
        rental.setActualReturnDate(LocalDate.now());

        when(rentalRepository.findByIdWithCarAndUser(rentalId)).thenReturn(Optional.of(rental));

        //When & Then
        RentalAlreadyReturnedException ex = assertThrows(
                RentalAlreadyReturnedException.class,
                () -> rentalService.returnRental(userId, rentalId)
        );

        assertThat(ex.getMessage()).contains("This rental has already been returned");

        verify(rentalRepository).findByIdWithCarAndUser(rentalId);
        verifyNoMoreInteractions(rentalRepository, rentalMapper,
                carRepository, applicationEventPublisher);
    }
}
