package com.carshare.rentalsystem.client.telegram.command.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;

public interface TelegramCommandHandler {
    String getCommand();

    void handle(TelegramBot bot, Message message);
}
