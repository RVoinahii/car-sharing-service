package com.carshare.rentalsystem.client.telegram.message.template.rental;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import org.springframework.stereotype.Component;

@Component
public class RentalStartMessageTemplateBuilder extends BaseRentalMessageBuilder<RentalResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.RENTAL_START_MSG;
    }

    @Override
    public Class<RentalResponseDto> getSupportedType() {
        return RentalResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, RentalResponseDto context) {
        Long rentalId = context.getId();
        Long userId = context.getUser().getId();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format("""
            ✅ Your rental is now ACTIVE!

            📋 Rental ID: %s

            Enjoy your ride, and drive safely! 🛣️
            """,
                    rentalId
            );
            case RECIPIENT_MANAGER -> String.format("""
            🔔 A rental has just become ACTIVE.

            📋 Rental ID: %s
            👤 User ID: %s
            """,
                    rentalId,
                    userId
            );
        };
    }
}
