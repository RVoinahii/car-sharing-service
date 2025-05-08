package com.carshare.rentalsystem.notifications.telegram.command.handler;

import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.notifications.telegram.ActiveTelegramUserStorage;
import com.carshare.rentalsystem.notifications.telegram.TelegramAuthenticationService;
import com.carshare.rentalsystem.notifications.telegram.TelegramLinkService;
import com.carshare.rentalsystem.notifications.telegram.notification.sender.templates.AuthenticationNotificationTemplates;
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
    private final AuthenticationNotificationTemplates authenticationNotificationTemplates;
    private final ActiveTelegramUserStorage activeUserLinks;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void handle(TelegramBot bot, Message message) {
        Long chatId = message.chat().id();
        String text = message.text();

        boolean isStartCommand = text != null && text.startsWith(START_COMMAND);

        if (isStartCommand) {
            if (text.length() == START_COMMAND.length()) {
                bot.execute(new SendMessage(chatId, "Welcome! Please authenticate"
                        + " yourself by logging in on our website."));
                return;
            }
        }

        if (isStartCommand) {
            String parameter = text.substring(START_COMMAND.length()).trim();
            if (telegramAuthenticationService.isLinkParameterValid(parameter)) {
                String username = message.from().firstName();
                String userId = AesEncryptionUtil.decrypt(parameter);
                if (telegramUserLinkRepository.existsByUserId(Long.parseLong(userId))) {
                    bot.execute(new SendMessage(chatId, "❗️ User already exist."));
                    return;
                }
                TelegramUserLink telegramUserLink = telegramLinkService.linkUser(
                        Long.parseLong(userId), chatId);
                bot.execute(new SendMessage(chatId,
                        authenticationNotificationTemplates
                                .createAuthenticationSuccessMessage(
                                        username, telegramUserLink.getUser())));
                activeUserLinks.add(telegramUserLink);
            } else {
                bot.execute(new SendMessage(chatId, "❗️ Invalid link."));
            }
        }
    }
}
