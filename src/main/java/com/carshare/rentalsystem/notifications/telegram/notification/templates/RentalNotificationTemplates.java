package com.carshare.rentalsystem.notifications.telegram.notification.templates;

import com.carshare.rentalsystem.model.Car;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.notifications.telegram.NotificationType;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RentalNotificationTemplates {
    public String createNewRentalMessage(NotificationType type, Rental rental) {
        User user = rental.getUser();
        Car car = rental.getCar();

        if (type == NotificationType.CUSTOMER) {
            return String.format("""
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
        } else {
            return String.format("""
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
        }
    }

    public String createRentalReturnMessage(NotificationType type, Rental rental) {
        User user = rental.getUser();
        Car car = rental.getCar();
        LocalDate expectedReturn = rental.getReturnDate();
        LocalDate actualReturn = rental.getActualReturnDate();
        boolean isLate = actualReturn.isAfter(expectedReturn);
        long delayDays = ChronoUnit.DAYS.between(expectedReturn, actualReturn);

        String statusCustomer = isLate ? "⚠️ Note: The return was after the expected date." : "";
        String statusManager = isLate
                ? "⚠️ Rental returned late!\n❗ Delay: " + delayDays + " day(s)"
                : "✅ Rental returned on time!";

        if (type == NotificationType.CUSTOMER) {
            return String.format("""
                            %s
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            """,
                    statusManager,
                    formatUserInfo(user),
                    formatCarInfo(car),
                    formatReturnedRentalInfo(rental)
            );
        } else {
            String status = isLate ? "⚠️ Rental returned late!" : "✅ Rental returned on time!";
            return String.format(
                    """
                            %s
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                               Brand: %s
                               Model: %s
                            
                            📝 Rental Info:
                            %s
                            
                            %s
                            """,
                    status,
                    formatUserInfo(user),
                    car.getBrand(),
                    car.getModel(),
                    formatReturnedRentalInfo(rental),
                    isLate ? "❗ Delay: " + delayDays + " day(s)" : ""
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
