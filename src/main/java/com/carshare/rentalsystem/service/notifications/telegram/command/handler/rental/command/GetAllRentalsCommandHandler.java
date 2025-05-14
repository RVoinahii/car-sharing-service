package com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command;

import com.carshare.rentalsystem.dto.rental.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.notifications.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.service.notifications.telegram.templates.RentalNotificationTemplates;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetAllRentalsCommandHandler implements TelegramCommandHandler {
    public static final String GET_ALL_RENTALS_COMMAND = "/get_all_rentals";
    public static final int PAGE_SIZE = 5;
    public static final String RENTALS_PAGE_CALLBACK_PREFIX = "rentals_page:";
    public static final int PAGE_INDEX_OFFSET = 1;

    private final RentalService rentalService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final RentalNotificationTemplates rentalNotificationTemplates;

    @Override
    public String getCommand() {
        return GET_ALL_RENTALS_COMMAND;
    }

    @Override
    public void handle(TelegramBot bot, Message message) {
        Long chatId = message.chat().id();
        TelegramUserLink telegramUserLink = telegramUserStorage.findByChatId(chatId).orElse(null);

        if (telegramUserLink == null) {
            bot.execute(new SendMessage(chatId,
                    "❗ Your Telegram account is not linked to any user in the system. "
                    + "Please register or contact support."));
            return;
        }

        User user = telegramUserLink.getUser();

        int pageNumber = 0;
        Page<RentalPreviewResponseDto> page = rentalService.getAllRentalsPreview(
                user.isManager(),
                telegramUserLink.getUserId(),
                PageRequest.of(pageNumber, PAGE_SIZE)
        );

        String response = rentalNotificationTemplates.createRentalListPageMessage(
                user.isCustomer(), page);

        SendMessage sendMessage = new SendMessage(chatId, response);
        sendMessage.replyMarkup(PaginationKeyboardBuilder.create(
                page.getNumber() + PAGE_INDEX_OFFSET,
                page.getTotalPages(),
                RENTALS_PAGE_CALLBACK_PREFIX)
        );

        bot.execute(sendMessage);
    }
}
