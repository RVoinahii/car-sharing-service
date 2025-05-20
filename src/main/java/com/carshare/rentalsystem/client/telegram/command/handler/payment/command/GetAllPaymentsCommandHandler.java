package com.carshare.rentalsystem.client.telegram.command.handler.payment.command;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_INDEX_OFFSET;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetAllRentalsCommandHandler.PAGE_SIZE;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.PaginationKeyboardBuilder;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.payment.stripe.StripePaymentService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetAllPaymentsCommandHandler implements TelegramCommandHandler {
    public static final String PAYMENTS_PAGE_CALLBACK_PREFIX = "payments_page:";

    private static final String GET_ALL_RENTAL_COMMAND = "/get_all_payments";

    private final StripePaymentService stripePaymentService;
    private final ActiveTelegramUserStorage telegramUserStorage;
    private final MessageTemplateDispatcher templateDispatcher;

    @Override
    public String getCommand() {
        return GET_ALL_RENTAL_COMMAND;
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
        Page<PaymentResponseDto> page = stripePaymentService.getAllPayments(
                user.isManager() ? null : user.getId(),
                PageRequest.of(pageNumber, PAGE_SIZE)
        );

        MessageRecipient recipient = user.isManager() ? MessageRecipient.RECIPIENT_MANAGER
                : MessageRecipient.RECIPIENT_CUSTOMER;

        String responseMessage = templateDispatcher.createMessage(
                MessageType.PAYMENT_LIST_MSG,
                recipient,
                page);

        SendMessage sendMessage = new SendMessage(chatId, responseMessage)
                .replyMarkup(PaginationKeyboardBuilder.create(
                        page.getNumber() + PAGE_INDEX_OFFSET,
                        page.getTotalPages(),
                        PAYMENTS_PAGE_CALLBACK_PREFIX)
                );

        bot.execute(sendMessage);
    }
}
