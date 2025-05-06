package com.carshare.rentalsystem.notifications.telegram;

import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.notifications.telegram.notification.templates.AuthenticationNotificationTemplates;
import com.carshare.rentalsystem.repository.telegram.user.link.TelegramUserLinkRepository;
import com.carshare.rentalsystem.util.AesEncryptionUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TelegramNotificationsService {
    public static final String MANAGER_ROLE = "MANAGER";
    private static final String START_COMMAND = "/start";

    private final TelegramUserLinkRepository telegramUserLinkRepository;
    private final TelegramAuthenticationService telegramAuthenticationService;
    private final TelegramLinkService telegramLinkService;
    private final AuthenticationNotificationTemplates authenticationNotificationTemplates;

    @Value("${telegram.bot-token}")
    private String botToken;

    private TelegramBot bot;

    private Set<TelegramUserLink> activeUserLinks = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        this.bot = new TelegramBot(botToken);
        List<TelegramUserLink> links = telegramUserLinkRepository.findAllWithUsersAndRoles();
        activeUserLinks.addAll(links);

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null) {
                    handleMessage(update.message());
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @Transactional
    public void notifyManagers(String messageText) {
        for (TelegramUserLink link : activeUserLinks) {
            if (MANAGER_ROLE.equals(link.getUser().getRole().getRole().name())) {
                try {
                    bot.execute(new SendMessage(link.getChatId(), messageText));
                } catch (Exception e) {
                    System.out.println("Failed to send to chat " + link.getChatId());
                }
            }
        }
    }

    @Transactional
    public void notifyCustomer(String messageText, Long userId) {
        for (TelegramUserLink link : activeUserLinks) {
            if (userId.equals(link.getUserId())) {
                try {
                    bot.execute(new SendMessage(link.getChatId(), messageText));
                } catch (Exception e) {
                    System.out.println("Failed to send to chat " + link.getChatId());
                }
            }
        }
    }

    private void handleMessage(Message message) {
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
