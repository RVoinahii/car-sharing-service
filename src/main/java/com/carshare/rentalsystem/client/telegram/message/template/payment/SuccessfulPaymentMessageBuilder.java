package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.car.response.dto.CarPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.dto.user.response.dto.UserPreviewResponseDto;
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
        RentalResponseDto rental = context.getRental();
        UserPreviewResponseDto user = rental.getUser();
        CarPreviewResponseDto car = rental.getCar();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format(
                    """
                            👋 Hello, %s!
                            
                            ✅ Payment completed successfully!
                            
                            👤 Customer:
                            %s
                            🚗 Car:
                            %s
                            🗓 Rental Period:
                               From: %s
                               To: %s
            
                            💳 Payment:
                               Payment ID: %s
                               Rental ID: %s
                               Amount Paid: %.2f USD
                               Type: %s
                               Status: %s
            
                            ⏳ Paid At: %s
                            """,
                    user.getFullName(),
                    formatUserInfo(user),
                    formatCarInfo(car),
                    rental.getRentalDate(),
                    rental.getActualReturnDate(),
                    context.getId(),
                    rental.getId(),
                    context.getAmountToPay(),
                    context.getType(),
                    context.getStatus(),
                    LocalDateTime.now()
            );
            case RECIPIENT_MANAGER -> String.format(
                    """
                            ✅ Payment completed successfully!
                            
                            👤 Customer:
                            %s
                            
                            🚗 Car:
                            %s
                            
                            📝 Rental Info:
                            %s
                            
                            💳 Payment Info:
                            %s
                            
                            📅 Created At: %s
                            💵 Paid At: %s
                            ⏳ Expires At: %s
                            """,
                    formatUserInfo(user),
                    formatCarInfo(car),
                    formatRentalInfo(rental),
                    formatFullPaymentInfo(context),
                    context.getCreatedAt(),
                    LocalDateTime.now(),
                    context.getExpiredAt()
            );
        };
    }
}
