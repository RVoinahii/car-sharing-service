package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
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
        RentalResponseDto rental = context.getRental();
        UserPreviewResponseDto user = rental.getUser();
        CarPreviewResponseDto car = rental.getCar();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format(
                    """
                            👋 Hello, %s!
                            ❌ Your payment was cancelled!
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            ⏳ Expires At: %s
                            
                            The session remains accessible for 24 hours after creation.
                            
                            Please contact support if you have any questions.
                            """,
                    user.getFullName(),
                    formatShortPaymentInfo(context),
                    context.getCreatedAt(),
                    context.getExpiredAt()
            );
            case RECIPIENT_MANAGER -> String.format(
                    """
                            ❌ Payment was cancelled!
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            ⏳ Expires At: %s
                            """,
                    formatUserInfo(user),
                    formatCarInfo(car),
                    formatRentalInfo(rental),
                    formatFullPaymentInfo(context),
                    context.getCreatedAt(),
                    context.getExpiredAt()
            );
        };
    }
}
