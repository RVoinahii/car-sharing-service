package com.carshare.rentalsystem.client.telegram.message.template.rental;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
import org.springframework.stereotype.Component;

@Component
public class RentalReturnMessageTemplateBuilder
        extends BaseRentalMessageBuilder<RentalResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.RENTAL_RETURN_MSG;
    }

    @Override
    public Class<RentalResponseDto> getSupportedType() {
        return RentalResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, RentalResponseDto context) {
        UserPreviewResponseDto user = context.getUser();
        CarPreviewResponseDto car = context.getCar();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format(""" 
                            👋 Hello, %s!
        
                            ✅ You have successfully placed your rental!
        
                            🚗 Car:
                            %s
        
                            📝 Rental Info:
                            %s
                            """,
                    user.getFullName(),
                    formatCarInfo(car),
                    formatActiveRentalInfo(context)
            );
            case RECIPIENT_MANAGER -> String.format(""" 
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
                    formatActiveRentalInfo(context)
            );
        };
    }
}
