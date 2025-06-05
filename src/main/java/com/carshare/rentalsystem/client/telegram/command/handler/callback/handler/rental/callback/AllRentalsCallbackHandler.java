package com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.rental.callback;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_SIZE;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.RENTALS_PAGE_CALLBACK_PREFIX;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.TelegramCallbackHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.rental.request.dto.RentalSearchParameters;
import com.carshare.rentalsystem.dto.rental.response.dto.RentalPreviewResponseDto;
import com.carshare.rentalsystem.model.Rental;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
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
    public static final String PAGE_SPLIT_DELIMITER = ":";
    public static final int FIRST_PAGE_INDEX = 0;

    private final RentalService rentalService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final MessageTemplateDispatcher templateDispatcher;

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
        String[] dataParts = callbackData.split(PAGE_SPLIT_DELIMITER);
        int pageNumber = parsePageNumber(dataParts);

        Page<RentalPreviewResponseDto> page;
        RentalSearchParameters searchParameters = null;

        if (user.isManager()) {
            searchParameters = parseSearchParametersFromCallbackParts(dataParts);
            page = rentalService.getSpecificRentals(
                    searchParameters,
                    PageRequest.of(pageNumber, PAGE_SIZE)
            );
        } else {
            page = rentalService.getRentalsById(
                    telegramUserLink.getUserId(),
                    PageRequest.of(pageNumber, PAGE_SIZE)
            );
        }

        MessageRecipient recipient = user.isManager()
                ? MessageRecipient.RECIPIENT_MANAGER
                : MessageRecipient.RECIPIENT_CUSTOMER;

        String response = templateDispatcher.createMessage(
                MessageType.RENTAL_LIST_MSG,
                recipient,
                page);

        String userIdPart = null;
        String statusPart = null;

        if (searchParameters != null) {
            userIdPart = searchParameters.userId();
            statusPart = searchParameters.status() != null
                    ? searchParameters.status().name()
                    : null;
        }

        InlineKeyboardMarkup keyboardMarkup = PaginationKeyboardBuilder.create(
                page.getNumber() + PAGE_INDEX_OFFSET,
                page.getTotalPages(),
                RENTALS_PAGE_CALLBACK_PREFIX,
                userIdPart,
                statusPart);

        EditMessageText editMessage = new EditMessageText(chatId, message.messageId(), response)
                .replyMarkup(keyboardMarkup);

        bot.execute(editMessage);
    }

    private int parsePageNumber(String[] dataParts) {
        try {
            return Integer.parseInt(dataParts[1]) - PAGE_INDEX_OFFSET;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return FIRST_PAGE_INDEX;
        }
    }

    private RentalSearchParameters parseSearchParametersFromCallbackParts(String[] dataParts) {
        String userId = null;
        Rental.RentalStatus status = null;

        for (int i = 2; i < dataParts.length; i++) {
            String token = dataParts[i].trim();
            if (token.isEmpty()) {
                continue;
            }
            try {
                status = Rental.RentalStatus.valueOf(token.toUpperCase());
            } catch (IllegalArgumentException e) {
                userId = token;
            }
        }

        return new RentalSearchParameters(userId, status);
    }
}
