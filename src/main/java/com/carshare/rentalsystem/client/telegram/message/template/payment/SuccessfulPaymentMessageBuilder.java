package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SuccessfulPaymentMessageBuilder
        extends BasePaymentMessageBuilder<PaymentResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT_SUCCESS_MSG;
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
                            ✅ Payment completed successfully!
                            
                            📝 Rental Info:
                            %s
            
                            💳 Payment:
                            %s
            
                            ⏳ Paid At: %s
                            """,
                    formatRentalInfo(rental),
                    formatPaymentInfo(context),
                    LocalDateTime.now()
            );
            case RECIPIENT_MANAGER -> String.format(
                    """
                            ✅ Customer payment completed successfully!
                            
                            📝 Rental Info:
                            %s
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            💵 Paid At: %s
                            ⏳ Expires At: %s
                            """,
                    formatRentalInfo(rental),
                    formatPaymentInfo(context),
                    context.getCreatedAt(),
                    LocalDateTime.now(),
                    context.getExpiredAt()
            );
        };
    }
}
