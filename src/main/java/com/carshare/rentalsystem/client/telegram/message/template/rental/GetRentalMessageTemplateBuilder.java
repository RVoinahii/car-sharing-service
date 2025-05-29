package com.carshare.rentalsystem.client.telegram.message.template.rental;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class GetRentalMessageTemplateBuilder extends BaseRentalMessageBuilder<RentalResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.RENTAL_INFO_MSG;
    }

    @Override
    public Class<RentalResponseDto> getSupportedType() {
        return RentalResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, RentalResponseDto context) {
        CarPreviewResponseDto car = context.getCar();
        LocalDate expectedReturn = context.getReturnDate();
        LocalDate actualReturn = context.getActualReturnDate();

        boolean isReturned = actualReturn != null;
        boolean isLate = isReturned && actualReturn.isAfter(expectedReturn);

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format("""
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
                    Rental Status: %s
                """,
                    car.getBrand(),
                    car.getModel(),
                    car.getType(),
                    context.getId(),
                    context.getRentalDate(),
                    context.getReturnDate(),
                    context.getActualReturnDate(),
                    context.getStatus()
            );
            case RECIPIENT_MANAGER -> String.format("""
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
                """,
                    context.getUser().getId(),
                    car.getBrand(),
                    car.getModel(),
                    car.getType(),
                    context.getId(),
                    context.getRentalDate(),
                    context.getReturnDate(),
                    context.getActualReturnDate()
            );
        };
    }
}
