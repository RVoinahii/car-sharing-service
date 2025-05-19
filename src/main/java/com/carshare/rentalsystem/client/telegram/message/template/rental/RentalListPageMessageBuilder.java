package com.carshare.rentalsystem.client.telegram.message.template.rental;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RentalListPageMessageBuilder
        extends BaseRentalMessageBuilder<Page<RentalResponseDto>> {
    @Override
    public MessageType getMessageType() {
        return MessageType.RENTAL_LIST_MSG;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Page<RentalResponseDto>> getSupportedType() {
        return (Class<Page<RentalResponseDto>>) (Class<?>) Page.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient, Page<RentalResponseDto> context) {
        StringBuilder builder = new StringBuilder();
        int currentPage = context.getNumber() + PAGE_INDEX_OFFSET;
        int totalPages = context.getTotalPages();

        builder.append(String.format("📋 Rentals — page %d of %d:\n\n", currentPage, totalPages));

        for (RentalResponseDto rental : context.getContent()) {
            builder.append("🔹 Rental ID: ")
                    .append(rental.getId())
                    .append(" — ")
                    .append(rental.getActualReturnDate() != null ? "▶ Active" : "✅ Completed");

            if (recipient.name().equals(MessageRecipient.RECIPIENT_MANAGER.name())) {
                builder.append(" — 👤 User ID: ").append(rental.getUser().getId());
            }

            builder.append("\n");
        }

        builder.append("\n📎 To view details: /get_rental <rental_id>");
        return builder.toString();
    }
}
