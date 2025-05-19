package com.carshare.rentalsystem.client.telegram.command.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TelegramCommandDispatcher {
    private final Map<String, TelegramCommandHandler> handlers;

    public TelegramCommandDispatcher(List<TelegramCommandHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(TelegramCommandHandler::getCommand, handler -> handler));
    }

    public TelegramCommandHandler getCommandHandler(String key) {
        return handlers.get(key);
    }
}
