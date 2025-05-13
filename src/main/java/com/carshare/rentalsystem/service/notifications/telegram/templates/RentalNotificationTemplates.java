package com.carshare.rentalsystem.service.notifications.telegram.templates;

import com.carshare.rentalsystem.dto.car.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentalNotificationTemplates {
    public static final boolean IS_CUSTOMER_MESSAGE = true;
    public static final boolean IS_MANAGER_MESSAGE = false;

    public EnumMap<NotificationRecipient, String> createNewRentalMessages(Rental rental) {
        EnumMap<NotificationRecipient, String> messages = new EnumMap<>(
                NotificationRecipient.class);
        User user = rental.getUser();
        Car car = rental.getCar();

        String customerMessage = String.format("""
                            👋 Hello, %s!

                            ✅ You have successfully placed your rental!
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            """,
                user.getFirstName(),
                formatCarInfo(car),
                formatActiveRentalInfo(rental)
        );

        messages.put(NotificationRecipient.CUSTOMER, customerMessage);

        String managerMessage = String.format("""
                            ✅ A new rental has been placed!
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            """,
                formatUserInfo(user),
                formatCarInfo(car),
                formatActiveRentalInfo(rental)
        );

        messages.put(NotificationRecipient.MANAGER, managerMessage);

        return messages;
    }

    public EnumMap<NotificationRecipient, String> createRentalReturnMessages(Rental rental) {
        EnumMap<NotificationRecipient, String> messages = new EnumMap<>(
                NotificationRecipient.class);

        User user = rental.getUser();
        Car car = rental.getCar();
        LocalDate expectedReturn = rental.getReturnDate();
        LocalDate actualReturn = rental.getActualReturnDate();
        boolean isLate = actualReturn.isAfter(expectedReturn);
        long delayDays = ChronoUnit.DAYS.between(expectedReturn, actualReturn);

        String customerStatus = isLate
                ? "⚠️ Note: The return was after the expected date."
                : "✅ Thank you for returning the car on time!";

        String customerMessage = String.format("""
                            %s
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            """,
                customerStatus,
                formatUserInfo(user),
                formatCarInfo(car),
                formatReturnedRentalInfo(rental)
        );
        messages.put(NotificationRecipient.CUSTOMER, customerMessage);

        String managerStatus = isLate
                ? "⚠️ Rental returned late!\n❗ Delay: " + delayDays + " day(s)"
                : "✅ Rental returned on time!";

        String managerMessage = String.format("""
                            %s
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            """,
                managerStatus,
                formatUserInfo(user),
                formatCarInfo(car),
                formatReturnedRentalInfo(rental)
        );
        messages.put(NotificationRecipient.MANAGER, managerMessage);

        return messages;
    }

    public String createGetRentalResponseMessage(boolean isCustomerMessage,
                                                 RentalResponseDto responseDto) {
        CarPreviewResponseDto car = responseDto.getCar();
        LocalDate expectedReturn = responseDto.getReturnDate();
        LocalDate actualReturn = responseDto.getActualReturnDate();

        boolean isReturned = actualReturn != null;
        boolean isLate = isReturned && actualReturn.isAfter(expectedReturn);
        long delayDays = isLate ? ChronoUnit.DAYS.between(expectedReturn, actualReturn) : 0;

        String rentalStatus = isReturned
                ? isLate
                ? "⏰ Returned late (" + delayDays + " day(s) delay)"
                : "✅ Returned on time"
                : "📅 Rental is still active";

        if (isCustomerMessage) {
            return String.format("""
                ✅ Here is your rental details!
                
                🚗 Car:
                    Brand: %s
                    Model: %s
                    Type: %s

                📝 Rental Info:
                    Rental ID: %s
                    Rental Start: %s
                    Rental Expected End: %s
                    Rental Actual End: %s

                %s
                """,
                    car.getBrand(),
                    car.getModel(),
                    car.getType(),
                    responseDto.getId(),
                    responseDto.getRentalDate(),
                    responseDto.getReturnDate(),
                    responseDto.getActualReturnDate(),
                    rentalStatus
            );
        } else {
            return String.format("""
                👤 Customer:
                    ID: %s

                🚗 Car:
                    Brand: %s
                    Model: %s
                    Type: %s

                📝 Rental Info:
                    Rental ID: %s
                    Rental Start: %s
                    Rental Expected End: %s
                    Rental Actual End: %s

                %s
                """,
                    responseDto.getUserId(),
                    car.getBrand(),
                    car.getModel(),
                    car.getType(),
                    responseDto.getId(),
                    responseDto.getRentalDate(),
                    responseDto.getReturnDate(),
                    responseDto.getActualReturnDate(),
                    rentalStatus
            );
        }
    }

    private String formatUserInfo(User user) {
        return String.format("""
                               ID: %s
                               Name: %s %s
                               Email: %s
                            """,
                user.getId(),
                user.getFirstName(), user.getLastName(),
                user.getEmail());
    }

    private String formatCarInfo(Car car) {
        return String.format("""
                               Brand: %s
                               Model: %s
                               Daily fee: %.2f USD
                            """,
                car.getBrand(),
                car.getModel(),
                car.getDailyFee());
    }

    private String formatActiveRentalInfo(Rental rental) {
        return String.format("""
                               Rental ID: %s
                               Rental Start: %s
                               Rental End: %s
                            """,
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate());
    }

    private String formatReturnedRentalInfo(Rental rental) {
        return String.format("""
                               Rental ID: %s
                               Rental Start: %s
                               Rental Expected End: %s
                               Rental Actual End: %s
                            """,
                rental.getId(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                rental.getActualReturnDate());
    }
}
