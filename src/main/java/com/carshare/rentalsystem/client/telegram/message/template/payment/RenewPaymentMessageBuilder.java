package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
import org.springframework.stereotype.Component;

@Component
public class RenewPaymentMessageBuilder extends BasePaymentMessageBuilder<PaymentResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT_RENEW_MSG;
    }

    @Override
    public Class<PaymentResponseDto> getSupportedType() {
        return PaymentResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, PaymentResponseDto context) {
        UserPreviewResponseDto user = context.getRental().getUser();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format(
                    """
                            👋 Hello, %s!
                            🔄 Your payment session has been renewed!
                            
                            💳 Payment Info:
                            %s
    
                            📅 Created At: %s
                            ⏳ Expiration Time: %s
    
                            You can now proceed with your payment.
                            Please complete it before the session expires.
                            
                            If you face any issues, please contact support.
                            """,
                    user.getFullName(),
                    formatShortPaymentInfo(context),
                    context.getCreatedAt(),
                    context.getExpiredAt()
            );
            case RECIPIENT_MANAGER -> String.format(
                    """
                            🔄 A payment session has been renewed by the customer.
                            
                            👤 Customer:
                            %s
                            
                            💳 Payment Info:
                            %s
        
                            📅 Created At: %s
                            ⏳ Expiration Time: %s
        
                            The customer has requested to renew the payment session.
                            Please monitor the status in case further assistance is needed.
                            """,
                    formatUserInfo(user),
                    formatShortPaymentInfo(context),
                    context.getCreatedAt(),
                    context.getExpiredAt()
            );
        };
    }
}
