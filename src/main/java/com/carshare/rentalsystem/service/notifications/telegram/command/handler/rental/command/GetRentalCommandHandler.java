package com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command;

import static com.carshare.rentalsystem.service.notifications.telegram.TelegramBotService.MANAGER_ROLE;

import com.carshare.rentalsystem.dto.rental.RentalResponseDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.service.notifications.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.service.notifications.telegram.templates.RentalNotificationTemplates;
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
    private final RentalNotificationTemplates rentalNotificationTemplates;

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

        try {
            Long rentalId = Long.parseLong(parts[RENTAL_ID_INDEX]);

            boolean isManager = telegramUserLink.getUser().getRole()
                    .getRole().name().equals(MANAGER_ROLE);

            if (isManager) {
                RentalResponseDto responseDto = rentalService.getAnyRentalInfo(rentalId);
                String responseMessage = rentalNotificationTemplates
                        .createGetRentalResponseMessage(
                                RentalNotificationTemplates.IS_MANAGER_MESSAGE, responseDto);
                bot.execute(new SendMessage(chatId, responseMessage));
            }

            RentalResponseDto responseDto = rentalService.getCustomerRentalInfo(
                    telegramUserLink.getUserId(), rentalId);
            String responseMessage = rentalNotificationTemplates
                    .createGetRentalResponseMessage(
                            RentalNotificationTemplates.IS_CUSTOMER_MESSAGE, responseDto);
            bot.execute(new SendMessage(chatId, responseMessage));
        } catch (NumberFormatException e) {
            bot.execute(new SendMessage(chatId, "❗️ Invalid rental ID. It must be a number."));
        } catch (EntityNotFoundException e) {
            bot.execute(new SendMessage(chatId, "❗️ Can't find rental with tha id"));
        }
    }
}
