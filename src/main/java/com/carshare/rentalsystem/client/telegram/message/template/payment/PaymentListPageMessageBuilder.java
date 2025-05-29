package com.carshare.rentalsystem.client.telegram.message.template.payment;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PaymentListPageMessageBuilder
        extends BasePaymentMessageBuilder<Page<PaymentResponseDto>> {
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT_LIST_MSG;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Page<PaymentResponseDto>> getSupportedType() {
        return (Class<Page<PaymentResponseDto>>) (Class<?>) Page.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, Page<PaymentResponseDto> context) {
        StringBuilder builder = new StringBuilder();
        int currentPage = context.getNumber() + PAGE_INDEX_OFFSET;
        int totalPages = context.getTotalPages();

        builder.append(String.format("📋 Payments — page %d of %d:\n\n", currentPage, totalPages));

        for (PaymentResponseDto payment : context.getContent()) {
            String emoji = formatStatusEmoji(Payment.PaymentStatus.valueOf(payment.getStatus()));

            builder.append("🔹 Payment ID: ")
                    .append(payment.getId())
                    .append(" — ")
                    .append(emoji)
                    .append(" ")
                    .append(payment.getStatus());

            if (recipient.name().equals(MessageRecipient.RECIPIENT_MANAGER.name())) {
                builder.append(" — 👤 User ID: ").append(payment.getRental().getUserId());
            }

            builder.append("\n");
        }

        builder.append("\n📎 To view details: /get_payment <payment_id>");
        return builder.toString();
    }
}
