package com.carshare.rentalsystem.client.telegram.command.handler.common.command;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.mapper.UserMapper;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.model.User;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class HelpCommandHandler implements TelegramCommandHandler {
    private static final String HELP_COMMAND = "/help";

    private final ActiveTelegramUserStorage telegramUserStorage;
    private final UserMapper userMapper;
    private final MessageTemplateDispatcher templateDispatcher;

    @Override
    public String getCommand() {
        return HELP_COMMAND;
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

        MessageRecipient recipient = user.isManager()
                ? MessageRecipient.RECIPIENT_MANAGER
                : MessageRecipient.RECIPIENT_CUSTOMER;

        UserResponseDto responseDto = userMapper.toDto(user);

        String responseMessage = templateDispatcher.createMessage(
                MessageType.COMMON_HELP_MSG,
                recipient,
                responseDto
        );

        bot.execute(new SendMessage(chatId, responseMessage));
    }
}
