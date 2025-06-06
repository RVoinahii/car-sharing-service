package com.carshare.rentalsystem.client.telegram.message.template.common;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CommonHelpMessageBuilder
        extends BaseCommonMessageBuilder<UserResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.COMMON_HELP_MSG;
    }

    @Override
    public Class<UserResponseDto> getSupportedType() {
        return UserResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, UserResponseDto context) {
        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format("""
        🤖 Hey %s! Here’s what you can do!
    
        📝 Rentals:
        /get_rental <id> — View your rental details by rental ID.
        /get_all_rentals — View your rentals list.
    
        💳 Payments:
        /get_payment <id> — View your payment details by payment ID.
        /get_all_payments — View your payment history.
    
        ℹ Other:
        /help — Show this help message.
    
        🔐 Note: You can only access your own rentals and payments.
            """,
                    context.getFirstName()
            );
            case RECIPIENT_MANAGER -> String.format("""
        🤖 Hey %s! Here’s what you can do!
    
        📝 Rentals:
        /get_rental <id> — View detailed info about a rental by its ID.
        /get_all_rentals [userId] [status] — View all rentals (filterable by user ID and status).
    
        💳 Payments:
        /get_payment <id> — View detailed info about a payment by its ID.
        /get_all_payments [userId] [status] — View all payments (filterable by user ID and status).
    
        ℹ Other:
        /help — Show this help message.
    
        🧠 Tip: Try filters like status=ACTIVE or status=WAITING_FOR_PAYMENT.
            """,
                    context.getFirstName()
            );
        };
    }
}
