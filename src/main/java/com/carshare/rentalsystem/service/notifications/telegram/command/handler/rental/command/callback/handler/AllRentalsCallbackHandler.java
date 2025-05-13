package com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.callback.handler;

import static com.carshare.rentalsystem.service.notifications.telegram.TelegramBotService.CUSTOMER_ROLE;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.EMPTY_ARRAY_SIZE;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.FIRST_PAGE_INDEX;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.FIRST_ROW_INDEX;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.NEXT_BUTTON_TEXT;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.NUMBER_OF_ROWS;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PREVIOUS_BUTTON_TEXT;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.RENTALS_PAGE_CALLBACK_PREFIX;

import com.carshare.rentalsystem.dto.rental.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.service.notifications.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler;
import com.carshare.rentalsystem.service.notifications.telegram.templates.RentalNotificationTemplates;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AllRentalsCallbackHandler {
    private static final String PAGE_SPLIT_DELIMITER = ":";
    private static final int PAGE_NUMBER_PART = 1;

    private final RentalService rentalService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final RentalNotificationTemplates rentalNotificationTemplates;

    public void handleCallback(TelegramBot bot, CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.message().chat().id();
        String callbackData = callbackQuery.data();

        TelegramUserLink telegramUserLink = telegramUserStorage.findByChatId(chatId).orElse(null);

        if (telegramUserLink == null) {
            bot.execute(new SendMessage(chatId, "❗ You are not linked."));
            return;
        }

        boolean isCustomer = telegramUserLink.getUser().getRole()
                .getRole().name().equals(CUSTOMER_ROLE);

        if (callbackData.startsWith(RENTALS_PAGE_CALLBACK_PREFIX)) {
            int pageNumber = Integer.parseInt(
                    callbackData
                            .split(PAGE_SPLIT_DELIMITER)[PAGE_NUMBER_PART]) - PAGE_INDEX_OFFSET;

            Page<RentalPreviewResponseDto> page = rentalService.getAllRentalsPreview(
                    isCustomer,
                    telegramUserLink.getUserId(),
                    PageRequest.of(pageNumber, GetAllRentalsCommandHandler.PAGE_SIZE)
            );

            String response = rentalNotificationTemplates.createRentalListPageMessage(
                    isCustomer, page);

            InlineKeyboardMarkup keyboardMarkup = createPaginationButtons(
                    page.getNumber() + PAGE_INDEX_OFFSET, page.getTotalPages());

            EditMessageText editMessage = new EditMessageText(chatId,
                    callbackQuery.message().messageId(), response);
            editMessage.replyMarkup(keyboardMarkup);

            bot.execute(editMessage);
        }
    }

    private InlineKeyboardMarkup createPaginationButtons(int currentPage, int totalPages) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (currentPage > FIRST_PAGE_INDEX) {
            buttons.add(new InlineKeyboardButton(PREVIOUS_BUTTON_TEXT)
                    .callbackData(
                            RENTALS_PAGE_CALLBACK_PREFIX + (currentPage - PAGE_INDEX_OFFSET)
                    )
            );
        }

        if (currentPage < totalPages) {
            buttons.add(new InlineKeyboardButton(NEXT_BUTTON_TEXT)
                    .callbackData(
                            RENTALS_PAGE_CALLBACK_PREFIX + (currentPage + PAGE_INDEX_OFFSET)
                    )
            );
        }

        InlineKeyboardButton[][] keyboard =
                new InlineKeyboardButton[NUMBER_OF_ROWS][buttons.size()];
        keyboard[FIRST_ROW_INDEX] = buttons.toArray(new InlineKeyboardButton[EMPTY_ARRAY_SIZE]);

        return new InlineKeyboardMarkup(keyboard);
    }
}
