package com.carshare.rentalsystem.client.telegram.message.template.payment;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PaymentExpiredMessageTemplateBuilder
        extends BasePaymentMessageBuilder<PaymentResponseDto> {
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT_EXPIRED_MSG;
    }

    @Override
    public Class<PaymentResponseDto> getSupportedType() {
        return PaymentResponseDto.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, PaymentResponseDto context) {
        Long paymentId = context.getId();
        Long userId = context.getRental().getUserId();

        return switch (recipient) {
            case RECIPIENT_CUSTOMER -> String.format("""
            ⚠️ Your payment session has EXPIRED!

            💳 Payment ID: %s

            Please create a new payment session or restore your previous one to continue.
            """,
                    paymentId
            );
            case RECIPIENT_MANAGER -> String.format("""
            🔔 A payment session has EXPIRED.

            💳 Payment ID: %s
            👤 User ID: %s

            The user needs to create or restore a payment session.
            """,
                    paymentId,
                    userId
            );
        };
    }
}
