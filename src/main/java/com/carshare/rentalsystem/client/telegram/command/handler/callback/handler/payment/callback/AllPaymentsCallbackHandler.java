package com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.payment.callback;

import static com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.rental.callback.AllRentalsCallbackHandler.FIRST_PAGE_INDEX;
import static com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.rental.callback.AllRentalsCallbackHandler.PAGE_NUMBER_PART;
import static com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.rental.callback.AllRentalsCallbackHandler.PAGE_SPLIT_DELIMITER;
import static com.carshare.rentalsystem.client.telegram.command.handler.payment.command.GetAllPaymentsCommandHandler.PAYMENTS_PAGE_CALLBACK_PREFIX;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_SIZE;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.TelegramCallbackHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.payment.stripe.StripePaymentService;
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
public class AllPaymentsCallbackHandler implements TelegramCallbackHandler {
    private final StripePaymentService stripePaymentService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final MessageTemplateDispatcher templateDispatcher;

    @Override
    public String getCallbackData() {
        return PAYMENTS_PAGE_CALLBACK_PREFIX;
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

        if (callbackData.startsWith(PAYMENTS_PAGE_CALLBACK_PREFIX)) {
            int pageNumber = parsePageNumber(callbackData);

            Page<PaymentResponseDto> page = stripePaymentService.getAllPayments(
                    user.isManager() ? null : user.getId(),
                    PageRequest.of(pageNumber, PAGE_SIZE)
            );

            MessageRecipient recipient = user.isManager() ? MessageRecipient.RECIPIENT_MANAGER
                    : MessageRecipient.RECIPIENT_CUSTOMER;

            String response = templateDispatcher.createMessage(
                    MessageType.PAYMENT_LIST_MSG,
                    recipient,
                    page);

            InlineKeyboardMarkup keyboardMarkup = PaginationKeyboardBuilder.create(
                    page.getNumber() + PAGE_INDEX_OFFSET,
                    page.getTotalPages(),
                    PAYMENTS_PAGE_CALLBACK_PREFIX);

            EditMessageText editMessage = new EditMessageText(chatId,
                    message.messageId(), response).replyMarkup(keyboardMarkup);

            bot.execute(editMessage);
        }
    }

    private int parsePageNumber(String callbackData) {
        try {
            return Integer.parseInt(callbackData
                    .split(PAGE_SPLIT_DELIMITER)[PAGE_NUMBER_PART]) - PAGE_INDEX_OFFSET;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return FIRST_PAGE_INDEX;
        }
    }
}
