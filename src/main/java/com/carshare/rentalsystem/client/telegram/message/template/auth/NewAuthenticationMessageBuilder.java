package com.carshare.rentalsystem.client.telegram.message.template.auth;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class NewAuthenticationMessageBuilder
        extends BaseAuthenticationMessageBuilder<UserResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_LINK_MSG;
    }

    @Override
    public Class<UserResponseDto> getSupportedType() {
        return UserResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, UserResponseDto context) {

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format(
                    """
                            ✅ You have successfully authenticated, %s!
                            
                            👤 Your details:
                            %s
                            """,
                    context.getFirstName(),
                    formatUserInfo(context)
            );
            case RECIPIENT_MANAGER -> String.format(
                    """
                            ✅ Manager %s, you have successfully authenticated!
                            
                            👤 Your details:
                            %s
                            """,
                    context.getFirstName(),
                    formatUserInfo(context)
            );
        };
    }
}
