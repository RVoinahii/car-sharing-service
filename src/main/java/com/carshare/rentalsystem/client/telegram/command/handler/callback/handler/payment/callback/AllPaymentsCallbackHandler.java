package com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.payment.callback;

import static com.carshare.rentalsystem.client.telegram.command.handler.callback.handler.rental.callback.AllRentalsCallbackHandler.FIRST_PAGE_INDEX;
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
import com.carshare.rentalsystem.dto.payment.request.dto.PaymentSearchParameters;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.model.Payment;
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
                    .text("⚠️ Your Telegram account is not linked to any user in our system "
                            + "Please register on our website.")
                    .showAlert(true));
            return;
        }

        User user = telegramUserLink.getUser();
        String[] dataParts = callbackData.split(PAGE_SPLIT_DELIMITER);
        int pageNumber = parsePageNumber(dataParts);

        Page<PaymentPreviewResponseDto> page;
        PaymentSearchParameters searchParameters = null;

        if (user.isManager()) {
            searchParameters = parseSearchParametersFromCallbackParts(dataParts);
            page = stripePaymentService.getSpecificPayments(
                    searchParameters,
                    PageRequest.of(pageNumber, PAGE_SIZE)
            );
        } else {
            page = stripePaymentService.getPaymentsByUserId(
                    telegramUserLink.getUserId(),
                    PageRequest.of(pageNumber, PAGE_SIZE)
            );
        }

        MessageRecipient recipient = user.isManager()
                ? MessageRecipient.RECIPIENT_MANAGER
                : MessageRecipient.RECIPIENT_CUSTOMER;

        String response = templateDispatcher.createMessage(
                MessageType.PAYMENT_LIST_MSG,
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
                PAYMENTS_PAGE_CALLBACK_PREFIX,
                userIdPart,
                statusPart);

        EditMessageText editMessage = new EditMessageText(chatId,
                message.messageId(), response).replyMarkup(keyboardMarkup);

        bot.execute(editMessage);
    }

    private int parsePageNumber(String[] dataParts) {
        try {
            return Integer.parseInt(dataParts[1]) - PAGE_INDEX_OFFSET;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return FIRST_PAGE_INDEX;
        }
    }

    private PaymentSearchParameters parseSearchParametersFromCallbackParts(String[] dataParts) {
        String userId = null;
        Payment.PaymentStatus status = null;

        for (int i = 2; i < dataParts.length; i++) {
            String token = dataParts[i].trim();
            if (token.isEmpty()) {
                continue;
            }
            try {
                status = Payment.PaymentStatus.valueOf(token.toUpperCase());
            } catch (IllegalArgumentException e) {
                userId = token;
            }
        }

        return new PaymentSearchParameters(userId, status);
    }
}
