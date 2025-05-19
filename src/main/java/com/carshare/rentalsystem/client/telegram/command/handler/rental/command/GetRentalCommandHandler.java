package com.carshare.rentalsystem.client.telegram.command.handler.rental.command;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalResponseDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetRentalCommandHandler implements TelegramCommandHandler {
    public static final String GET_RENTAL_COMMAND = "/get_rental";
    private static final String COMMAND_ARGUMENT_DELIMITER_REGEX = "\\s+";
    private static final int MIN_COMMAND_PARTS = 2;
    private static final int RENTAL_ID_INDEX = 1;

    private final RentalService rentalService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final MessageTemplateDispatcher templateDispatcher;

    @Override
    public String getCommand() {
        return GET_RENTAL_COMMAND;
    }

    @Override
    public void handle(TelegramBot bot, Message message) {
        Long chatId = message.chat().id();
        String text = message.text();
        TelegramUserLink telegramUserLink = telegramUserStorage.findByChatId(chatId).orElse(null);

        if (telegramUserLink == null) {
            bot.execute(new SendMessage(chatId,
                    "❗ Your Telegram account is not linked to any user in the system. "
                            + "Please register or contact support."));
            return;
        }

        String[] parts = text.trim().split(COMMAND_ARGUMENT_DELIMITER_REGEX);
        if (parts.length < MIN_COMMAND_PARTS) {
            bot.execute(new SendMessage(
                    chatId, "❗ Please provide a rental ID. Example: /my_rental 123")
            );
            return;
        }

        long rentalId;

        try {
            rentalId = Long.parseLong(parts[RENTAL_ID_INDEX]);
        } catch (NumberFormatException e) {
            bot.execute(new SendMessage(chatId, "❗️ Invalid rental ID. It must be a number."));
            return;
        }

        User user = telegramUserLink.getUser();

        try {
            MessageRecipient recipient = user.isManager() ? MessageRecipient.RECIPIENT_MANAGER
                    : MessageRecipient.RECIPIENT_CUSTOMER;

            RentalResponseDto responseDto = getResponseDtoForUser(
                    user, rentalId, telegramUserLink.getUserId()
            );

            String responseMessage = templateDispatcher.createMessage(
                    MessageType.RENTAL_INFO_MSG,
                    recipient,
                    responseDto
            );

            bot.execute(new SendMessage(chatId, responseMessage));
        } catch (EntityNotFoundException e) {
            bot.execute(new SendMessage(chatId, "❗️ Can't find rental with tha id"));
        }
    }

    private RentalResponseDto getResponseDtoForUser(User user, Long rentalId, Long userId) {
        if (user.isManager()) {
            return rentalService.getAnyRentalInfo(rentalId);
        } else {
            return rentalService.getCustomerRentalInfo(userId, rentalId);
        }
    }
}
