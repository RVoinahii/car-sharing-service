package com.carshare.rentalsystem.client.telegram.command.handler.start.command;

import com.carshare.rentalsystem.client.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.client.telegram.TelegramAuthenticationService;
import com.carshare.rentalsystem.client.telegram.TelegramLinkService;
import com.carshare.rentalsystem.client.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.client.telegram.message.template.MessageRecipient;
import com.carshare.rentalsystem.client.telegram.message.template.MessageTemplateDispatcher;
import com.carshare.rentalsystem.client.telegram.message.template.MessageType;
import com.carshare.rentalsystem.dto.user.response.dto.UserResponseDto;
import com.carshare.rentalsystem.mapper.UserMapper;
import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.repository.telegram.user.link.TelegramUserLinkRepository;
import com.carshare.rentalsystem.util.AesEncryptionUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartCommandHandler implements TelegramCommandHandler {
    private static final String START_COMMAND = "/start";

    private final TelegramAuthenticationService telegramAuthenticationService;
    private final TelegramUserLinkRepository telegramUserLinkRepository;
    private final TelegramLinkService telegramLinkService;
    private final UserMapper userMapper;
    private final MessageTemplateDispatcher messageTemplateDispatcher;
    private final ActiveTelegramUserStorage activeUserLinks;

    @Override
    public String getCommand() {
        return START_COMMAND;
    }

    @Override
    public void handle(TelegramBot bot, Message message) {
        Long chatId = message.chat().id();
        String text = message.text();

        if (text.length() == START_COMMAND.length()) {
            bot.execute(new SendMessage(chatId, "Welcome! Please authenticate"
                    + " yourself by logging in on our website."));
            return;
        }

        String parameter = text.substring(START_COMMAND.length()).trim();
        if (!telegramAuthenticationService.isLinkParameterValid(parameter)) {
            bot.execute(new SendMessage(chatId, "❗️ Invalid link."));
            return;
        }

        String decryptedIdParameter = AesEncryptionUtil.decrypt(parameter);
        Long userId = Long.parseLong(decryptedIdParameter);

        if (telegramUserLinkRepository.existsByUserId(userId)) {
            bot.execute(new SendMessage(chatId, "❗️ User already exists."));
            return;
        }

        TelegramUserLink telegramUserLink = telegramLinkService.linkUser(userId, chatId);
        UserResponseDto userDto = userMapper.toDto(telegramUserLink.getUser());

        MessageRecipient recipient = telegramUserLink.getUser().isManager()
                ? MessageRecipient.RECIPIENT_MANAGER : MessageRecipient.RECIPIENT_CUSTOMER;

        String responseMessage = messageTemplateDispatcher.createMessage(
                MessageType.AUTH_LINK_MSG,
                recipient,
                userDto
        );

        bot.execute(new SendMessage(chatId, responseMessage));
        activeUserLinks.add(telegramUserLink);
    }
}
