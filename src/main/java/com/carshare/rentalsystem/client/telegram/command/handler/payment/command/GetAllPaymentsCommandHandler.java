package com.carshare.rentalsystem.client.telegram.command.handler.payment.command;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.MAX_ARGUMENTS_FOR_REGULAR_USER;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_SIZE;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.START_PAGE_INDEX;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetRentalCommandHandler.COMMAND_ARGUMENT_DELIMITER_REGEX;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
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
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetAllPaymentsCommandHandler implements TelegramCommandHandler {
    public static final String PAYMENTS_PAGE_CALLBACK_PREFIX = "payments_page:";

    private static final String GET_ALL_PAYMENTS_COMMAND = "/get_all_payments";

    private final StripePaymentService stripePaymentService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final MessageTemplateDispatcher templateDispatcher;

    @Override
    public String getCommand() {
        return GET_ALL_PAYMENTS_COMMAND;
    }

    @Override
    public void handle(TelegramBot bot, Message message) {
        Long chatId = message.chat().id();
        TelegramUserLink telegramUserLink = telegramUserStorage.findByChatId(chatId).orElse(null);

        if (telegramUserLink == null) {
            bot.execute(new SendMessage(chatId,
                    "⚠️ Your Telegram account is not linked to any user in our system "
                            + "Please register on our website."));
            return;
        }

        User user = telegramUserLink.getUser();

        String[] searchFilters = message.text().split(COMMAND_ARGUMENT_DELIMITER_REGEX);

        if (!user.isManager() && searchFilters.length > MAX_ARGUMENTS_FOR_REGULAR_USER) {
            bot.execute(new SendMessage(chatId,
                    "❗ Sorry, filtering rentals is currently available to managers only."));
            return;
        }

        Page<PaymentPreviewResponseDto> page;
        PaymentSearchParameters searchParameters = null;

        if (user.isManager()) {
            searchParameters = parseFilters(searchFilters);
            page = stripePaymentService.getSpecificPayments(
                    searchParameters,
                    PageRequest.of(START_PAGE_INDEX, PAGE_SIZE)
            );
        } else {
            page = stripePaymentService.getPaymentsByUserId(
                    telegramUserLink.getUserId(),
                    PageRequest.of(START_PAGE_INDEX, PAGE_SIZE)
            );
        }

        if (page.getContent().isEmpty()) {
            bot.execute(new SendMessage(chatId, "❗ No payments found for your request."));
            return;
        }

        String responseMessage = templateDispatcher.createMessage(
                MessageType.PAYMENT_LIST_MSG,
                user.isManager()
                        ? MessageRecipient.RECIPIENT_MANAGER
                        : MessageRecipient.RECIPIENT_CUSTOMER,
                page);

        SendMessage sendMessage = new SendMessage(chatId, responseMessage)
                .replyMarkup(PaginationKeyboardBuilder.create(
                        page.getNumber() + PAGE_INDEX_OFFSET,
                        page.getTotalPages(),
                        PAYMENTS_PAGE_CALLBACK_PREFIX,
                        filterNulls(
                                searchParameters.userId(),
                                searchParameters.status() != null
                                        ? searchParameters.status().name()
                                        : null)
                        ));

        bot.execute(sendMessage);
    }

    private PaymentSearchParameters parseFilters(String[] searchFilters) {
        String userIdFilter = null;
        Payment.PaymentStatus statusFilter = null;

        for (int i = 1; i < searchFilters.length; i++) {
            String token = searchFilters[i].trim();
            if (isStatusValue(token)) {
                statusFilter = Payment.PaymentStatus.valueOf(token.toUpperCase());
            } else {
                userIdFilter = token;
            }
        }
        return new PaymentSearchParameters(userIdFilter, statusFilter);
    }

    private boolean isStatusValue(String token) {
        return Arrays.stream(Payment.PaymentStatus.values())
                .anyMatch(status -> status.name().equalsIgnoreCase(token));
    }

    private String[] filterNulls(String... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }
}
