package com.carshare.rentalsystem.service.notifications.telegram.command.handler.callback.handler.rental.callback;

import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_SIZE;
import static com.carshare.rentalsystem.service.notifications.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.RENTALS_PAGE_CALLBACK_PREFIX;

import com.carshare.rentalsystem.dto.rental.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.notifications.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.callback.handler.TelegramCallbackHandler;
import com.carshare.rentalsystem.service.notifications.telegram.templates.RentalNotificationTemplates;
import com.carshare.rentalsystem.service.rental.RentalService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AllRentalsCallbackHandler implements TelegramCallbackHandler {
    private static final String PAGE_SPLIT_DELIMITER = ":";
    private static final int PAGE_NUMBER_PART = 1;

    private final RentalService rentalService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final RentalNotificationTemplates rentalNotificationTemplates;

    @Override
    public String getCallbackData() {
        return RENTALS_PAGE_CALLBACK_PREFIX;
    }

    @Override
    public void handle(TelegramBot bot, CallbackQuery callbackQuery) {
        Message message = callbackQuery.message();
        if (message == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id())
                    .text("⚠️ Unable to process this action.")
                    .showAlert(true));
            return;
        }

        Long chatId = message.chat().id();
        String callbackData = callbackQuery.data();

        TelegramUserLink telegramUserLink = telegramUserStorage.findByChatId(chatId).orElse(null);

        if (telegramUserLink == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id())
                    .text("❗ Your Telegram account is not linked to any user in the system. "
                            + "Please register or contact support.")
                    .showAlert(true));
            return;
        }

        User user = telegramUserLink.getUser();

        if (callbackData.startsWith(RENTALS_PAGE_CALLBACK_PREFIX)) {
            int pageNumber = Integer.parseInt(
                    callbackData
                            .split(PAGE_SPLIT_DELIMITER)[PAGE_NUMBER_PART]) - PAGE_INDEX_OFFSET;

            Page<RentalPreviewResponseDto> page = rentalService.getAllRentalsPreview(
                    user.isManager(),
                    telegramUserLink.getUserId(),
                    PageRequest.of(pageNumber, PAGE_SIZE)
            );

            String response = rentalNotificationTemplates.createRentalListPageMessage(
                    user.isCustomer(), page);

            InlineKeyboardMarkup keyboardMarkup = PaginationKeyboardBuilder.create(
                    page.getNumber() + PAGE_INDEX_OFFSET,
                    page.getTotalPages(),
                    RENTALS_PAGE_CALLBACK_PREFIX);

            EditMessageText editMessage = new EditMessageText(chatId,
                    message.messageId(), response);
            editMessage.replyMarkup(keyboardMarkup);

            bot.execute(editMessage);
        }
    }
}
