package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CancelPaymentMessageBuilder extends BasePaymentMessageBuilder<PaymentResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT_CANCEL_MSG;
    }

    @Override
    public Class<PaymentResponseDto> getSupportedType() {
        return PaymentResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, PaymentResponseDto context) {
        RentalPreviewResponseDto rental = context.getRental();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format(
                    """
                            ❌ Your payment was cancelled!
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            ⏳ Expires At: %s
                            
                            The session remains accessible for 24 hours after creation.
                            
                            Please contact support if you have any questions.
                            """,
                    formatLitePaymentInfo(context),
                    context.getCreatedAt(),
                    context.getExpiredAt()
            );
            case RECIPIENT_MANAGER -> String.format(
                    """
                            ❌ Customer payment was cancelled!
                            
                            📝 Rental Info:
                            %s
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            ⏳ Expires At: %s
                            """,
                    formatRentalInfo(rental),
                    formatPaymentInfo(context),
                    context.getCreatedAt(),
                    context.getExpiredAt()
            );
        };
    }
}
