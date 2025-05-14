package com.carshare.rentalsystem.service.notifications.telegram.command.handler.callback.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;

public interface TelegramCallbackHandler {
    String getCallbackData();

    void handle(TelegramBot bot, CallbackQuery callbackQuery);
}
