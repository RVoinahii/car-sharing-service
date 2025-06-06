package com.carshare.rentalsystem.client.telegram.message.template.rental;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;

import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RentalListPageMessageBuilder
        extends BaseRentalMessageBuilder<Page<RentalPreviewResponseDto>> {
    @Override
    public MessageType getMessageType() {
        return MessageType.RENTAL_LIST_MSG;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Page<RentalPreviewResponseDto>> getSupportedType() {
        return (Class<Page<RentalPreviewResponseDto>>) (Class<?>) Page.class;
    }

    @Override
    public String createMessage(MessageRecipient recipient,
                                Page<RentalPreviewResponseDto> context) {
        StringBuilder builder = new StringBuilder();
        int currentPage = context.getNumber() + PAGE_INDEX_OFFSET;
        int totalPages = context.getTotalPages();

        builder.append(String.format("📋 Rentals — page %d of %d:\n\n", currentPage, totalPages));

        for (RentalPreviewResponseDto rental : context.getContent()) {
            String emoji = formatStatusEmoji(Rental.RentalStatus.valueOf(rental.getStatus()));

            builder.append("🔹 Rental ID: ")
                    .append(rental.getId())
                    .append(" — ")
                    .append(emoji)
                    .append(" ")
                    .append(rental.getStatus());

            if (recipient.name().equals(MessageRecipient.RECIPIENT_MANAGER.name())) {
                builder.append(" — 👤 User ID: ").append(rental.getUserId());
            }

            builder.append("\n");
        }

        builder.append("\n📎 To view details: /get_rental <rental_id>");
        return builder.toString();
    }
}
