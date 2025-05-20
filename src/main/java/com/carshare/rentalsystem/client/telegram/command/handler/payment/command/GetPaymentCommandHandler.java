package com.carshare.rentalsystem.client.telegram.command.handler.payment.command;

import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetRentalCommandHandler.COMMAND_ARGUMENT_DELIMITER_REGEX;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetRentalCommandHandler.DATA_ID_INDEX;
import static com.carshare.rentalsystem.client.telegram.command.handler.rental.command.GetRentalCommandHandler.MIN_COMMAND_PARTS;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.exception.EntityNotFoundException;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.payment.stripe.StripePaymentService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetPaymentCommandHandler implements TelegramCommandHandler {
    private static final String GET_RENTAL_COMMAND = "/get_payment";

    private final ActiveTelegramUserStorage telegramUserStorage;
    private final StripePaymentService stripePaymentService;
    private final MessageTemplateDispatcher templateDispatcher;

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
                    chatId, "❗ Please provide a payment ID. Example: /get_payment 123")
            );
            return;
        }

        long paymentId;

        try {
            paymentId = Long.parseLong(parts[DATA_ID_INDEX]);
        } catch (NumberFormatException e) {
            bot.execute(new SendMessage(chatId, "❗️ Invalid payment ID. It must be a number."));
            return;
        }

        User user = telegramUserLink.getUser();

        try {
            MessageRecipient recipient = user.isManager() ? MessageRecipient.RECIPIENT_MANAGER
                    : MessageRecipient.RECIPIENT_CUSTOMER;

            PaymentResponseDto responseDto = getResponseDtoForUser(
                    user, paymentId, telegramUserLink.getUserId()
            );

            String responseMessage = templateDispatcher.createMessage(
                    MessageType.PAYMENT_INFO_MSG,
                    recipient,
                    responseDto
            );

            bot.execute(new SendMessage(chatId, responseMessage));
        } catch (EntityNotFoundException e) {
            bot.execute(new SendMessage(chatId, "❗️ Can't find payment with that id"));
        }
    }

    private PaymentResponseDto getResponseDtoForUser(User user, Long paymentId, Long userId) {
        if (user.isManager()) {
            return stripePaymentService.getAnyPaymentInfo(paymentId);
        } else {
            return stripePaymentService.getCustomerPaymentInfo(userId, paymentId);
        }
    }
}
