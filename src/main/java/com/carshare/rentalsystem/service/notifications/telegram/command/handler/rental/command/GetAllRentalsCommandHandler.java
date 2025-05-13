package com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command;

import static com.carshare.rentalsystem.service.notifications.telegram.TelegramBotService.CUSTOMER_ROLE;

import com.carshare.rentalsystem.dto.rental.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.service.notifications.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.service.notifications.telegram.templates.RentalNotificationTemplates;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetAllRentalsCommandHandler implements TelegramCommandHandler {
    public static final String GET_ALL_RENTALS_COMMAND = "/get_all_rentals";
    public static final int PAGE_SIZE = 5;
    public static final String PREVIOUS_BUTTON_TEXT = "⬅️ Previous";
    public static final String NEXT_BUTTON_TEXT = "➡️ Next";
    public static final String RENTALS_PAGE_CALLBACK_PREFIX = "rentals_page:";
    public static final int PAGE_INDEX_OFFSET = 1;
    public static final int FIRST_PAGE_INDEX = 1;
    public static final int NUMBER_OF_ROWS = 1;
    public static final int FIRST_ROW_INDEX = 0;
    public static final int EMPTY_ARRAY_SIZE = 0;

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
            bot.execute(new SendMessage(chatId, "❗ You are not linked."));
            return;
        }

        boolean isCustomer = telegramUserLink.getUser().getRole()
                .getRole().name().equals(CUSTOMER_ROLE);

        int pageNumber = 0;
        Page<RentalPreviewResponseDto> page = rentalService.getAllRentalsPreview(
                isCustomer,
                telegramUserLink.getUserId(),
                PageRequest.of(pageNumber, PAGE_SIZE)
        );

        String response = rentalNotificationTemplates.createRentalListPageMessage(isCustomer, page);

        SendMessage sendMessage = new SendMessage(chatId, response);
        sendMessage.replyMarkup(createPaginationButtons(
                page.getNumber() + PAGE_INDEX_OFFSET, page.getTotalPages()));

        bot.execute(sendMessage);
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
                    .callbackData(RENTALS_PAGE_CALLBACK_PREFIX + (currentPage + PAGE_INDEX_OFFSET)
                    )
            );
        }

        InlineKeyboardButton[][] keyboard =
                new InlineKeyboardButton[NUMBER_OF_ROWS][buttons.size()];
        keyboard[FIRST_ROW_INDEX] = buttons.toArray(new InlineKeyboardButton[EMPTY_ARRAY_SIZE]);

        return new InlineKeyboardMarkup(keyboard);
    }
}
