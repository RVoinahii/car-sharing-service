package com.carshare.rentalsystem.service.notifications.telegram;

import com.carshare.rentalsystem.model.TelegramUserLink;
import com.carshare.rentalsystem.repository.telegram.user.link.TelegramUserLinkRepository;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.TelegramCommandDispatcher;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.TelegramCommandHandler;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.callback.handler.TelegramCallbackDispatcher;
import com.carshare.rentalsystem.service.notifications.telegram.command.handler.callback.handler.TelegramCallbackHandler;
import com.carshare.rentalsystem.service.notifications.telegram.templates.NotificationRecipient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramBotService {
    private static final String COMMAND_PREFIX = "/";
    private static final String SPACE_DELIMITER = " ";
    private static final String COLON_DELIMITER = ":";
    private static final int COMMAND_PART_INDEX = 0;

    @Value("${telegram.bot-token}")
    private String botToken;

    private TelegramBot bot;

    private final TelegramUserLinkRepository telegramUserLinkRepository;
    private final TelegramCommandDispatcher telegramCommandDispatcher;
    private final TelegramCallbackDispatcher telegramCallbackDispatcher;
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
                } else if (update.callbackQuery() != null) {
                    handleCallback(update.callbackQuery());
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void notifyManagers(String messageText) {
        for (TelegramUserLink link : activeUserLinks.getAll()) {
            if (link.getUser().isManager()) {
                try {
                    bot.execute(new SendMessage(link.getChatId(), messageText));
                } catch (Exception e) {
                    System.out.println("Failed to send to chat " + link.getChatId());
                }
            }
        }
    }

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

    public void notifyRecipients(EnumMap<NotificationRecipient, String> messages,
                                 Long customerUserId) {
        if (messages.containsKey(NotificationRecipient.MANAGER)) {
            notifyManagers(messages.get(NotificationRecipient.MANAGER));
        }
        if (messages.containsKey(NotificationRecipient.CUSTOMER) && customerUserId != null) {
            notifyCustomer(messages.get(NotificationRecipient.CUSTOMER), customerUserId);
        }
    }

    private void handleMessage(Message message) {
        String text = message.text();

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

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        if (data != null) {
            String handlerKey = data.substring(0, data.indexOf(COLON_DELIMITER) + 1);
            TelegramCallbackHandler callbackHandler =
                    telegramCallbackDispatcher.getCallbackHandler(handlerKey);
            if (callbackHandler != null) {
                callbackHandler.handle(bot, callbackQuery);
            } else {
                Message message = callbackQuery.message();
                if (message == null) {
                    bot.execute(new AnswerCallbackQuery(callbackQuery.id())
                            .text("⚠️ Unable to process this action.")
                            .showAlert(true));
                    return;
                }
                bot.execute(new SendMessage(message.chat().id(), "❗️ Unknown callback"));
            }
        }
    }
}
