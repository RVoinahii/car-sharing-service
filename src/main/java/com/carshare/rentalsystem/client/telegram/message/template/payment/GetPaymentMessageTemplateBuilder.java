package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class GetPaymentMessageTemplateBuilder
        extends BasePaymentMessageBuilder<PaymentResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT_INFO_MSG;
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
                            ✅ Here is your rental details!
                            
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
        };
    }
}
