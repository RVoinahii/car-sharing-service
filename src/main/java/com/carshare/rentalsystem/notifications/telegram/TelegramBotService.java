package com.carshare.rentalsystem.notifications.telegram;

import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.notifications.telegram.command.handler.TelegramCommandDispatcher;
import com.carshare.rentalsystem.notifications.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.repository.telegram.user.link.TelegramUserLinkRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TelegramBotService {
    private static final String MANAGER_ROLE = "MANAGER";
    private static final String COMMAND_PREFIX = "/";
    private static final String SPACE_DELIMITER = " ";
    private static final int COMMAND_PART_INDEX = 0;

    @Value("${telegram.bot-token}")
    private String botToken;

    private TelegramBot bot;

    private final TelegramUserLinkRepository telegramUserLinkRepository;
    private final TelegramCommandDispatcher telegramCommandDispatcher;
    private final ActiveTelegramUserStorage activeUserLinks;

    @PostConstruct
    public void init() {
        this.bot = new TelegramBot(botToken);
        List<TelegramUserLink> links = telegramUserLinkRepository.findAllWithUsersAndRoles();
        activeUserLinks.refreshAll(links);

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
        for (TelegramUserLink link : activeUserLinks.getAll()) {
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
        for (TelegramUserLink link : activeUserLinks.getAll()) {
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
        String text = message.text();
        System.out.println(text);

        if (text != null && text.startsWith(COMMAND_PREFIX)) {
            String command = text.split(SPACE_DELIMITER)[COMMAND_PART_INDEX];

            TelegramCommandHandler commandHandler =
                    telegramCommandDispatcher.getCommandHandler(command);
            if (commandHandler != null) {
                commandHandler.handle(bot, message);
            } else {
                bot.execute(new SendMessage(message.chat().id(), "❗️ Unknown command"));
            }
        }
    }
}
